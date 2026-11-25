package fr.takit.reviewer.application;

import fr.takit.reviewer.config.ReviewerProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Daily limits that protect the free inference credit: a number of reviews per caller and a total
 * for the whole application. Kept in memory: a restart resets the counters, which is acceptable
 * for a demo running on a single instance.
 */
@Component
public class DailyQuota {

    private final int perCaller;
    private final int total;
    private final Clock clock;
    private final Map<String, AtomicInteger> byCaller = new ConcurrentHashMap<>();
    private final AtomicInteger used = new AtomicInteger();
    private volatile LocalDate day;

    public DailyQuota(ReviewerProperties properties, Clock clock) {
        this.perCaller = properties.reviewsPerCallerPerDay();
        this.total = properties.reviewsPerDay();
        this.clock = clock;
        this.day = LocalDate.now(clock);
    }

    public synchronized void consume(String caller) {
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(day)) {
            day = today;
            byCaller.clear();
            used.set(0);
        }
        if (used.get() >= total) {
            throw new QuotaExceededException(
                    "The free reviews of the day are used up. Come back tomorrow, or add your own API key.");
        }
        AtomicInteger mine = byCaller.computeIfAbsent(caller == null ? "unknown" : caller, k -> new AtomicInteger());
        if (mine.get() >= perCaller) {
            throw new QuotaExceededException(
                    "You reached " + perCaller + " free reviews today. Add your own API key to continue.");
        }
        mine.incrementAndGet();
        used.incrementAndGet();
    }
}
