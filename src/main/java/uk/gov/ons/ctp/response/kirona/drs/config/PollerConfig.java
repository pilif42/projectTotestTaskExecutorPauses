package uk.gov.ons.ctp.response.kirona.drs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("poller")
@Data
public class PollerConfig {
    private boolean fixedRate;

    private int maxMessagesPerPoll;
    private int supportHourStart;
    private int supportMinuteStart;
    private int supportHourEnd;

    private long fixedDelay;
    private long initialDelay;
}
