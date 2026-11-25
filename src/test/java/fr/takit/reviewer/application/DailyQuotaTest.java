package fr.takit.reviewer.application;

import fr.takit.reviewer.config.ReviewerProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DailyQuotaTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-25T10:00:00Z"));

    @Test
    void limitsEachCallerPerDay() {
        DailyQuota quota = new DailyQuota(new ReviewerProperties("Test", 2, 100), clock);
        quota.consume("1.2.3.4");
        quota.consume("1.2.3.4");

        assertThatThrownBy(() -> quota.consume("1.2.3.4"))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("2 free reviews");
        assertThatCode(() -> quota.consume("5.6.7.8")).doesNotThrowAnyException();
    }

    @Test
    void limitsTheWholeApplicationPerDay() {
        DailyQuota quota = new DailyQuota(new ReviewerProperties("Test", 10, 2), clock);
        quota.consume("a");
        quota.consume("b");

        assertThatThrownBy(() -> quota.consume("c")).hasMessageContaining("used up");
    }

    @Test
    void startsAgainTheNextDay() {
        DailyQuota quota = new DailyQuota(new ReviewerProperties("Test", 1, 1), clock);
        quota.consume("a");
        clock.advance(Duration.ofDays(1));

        assertThatCode(() -> quota.consume("a")).doesNotThrowAnyException();
    }

    static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
