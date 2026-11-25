package fr.takit.reviewer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings of the reviewer, prefix {@code reviewer}.
 *
 * @param providerName           name of the model provider, shown on the page
 * @param reviewsPerCallerPerDay free reviews per caller (IP address) and per day
 * @param reviewsPerDay          free reviews per day for the whole application
 */
@ConfigurationProperties("reviewer")
public record ReviewerProperties(
        @DefaultValue("OpenRouter") String providerName,
        @DefaultValue("5") int reviewsPerCallerPerDay,
        @DefaultValue("40") int reviewsPerDay) {
}
