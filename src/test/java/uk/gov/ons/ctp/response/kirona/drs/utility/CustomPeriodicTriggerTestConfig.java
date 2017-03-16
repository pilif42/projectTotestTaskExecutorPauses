package uk.gov.ons.ctp.response.kirona.drs.utility;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.ons.ctp.response.kirona.drs.config.PollerConfig;

@SpringBootConfiguration
public class CustomPeriodicTriggerTestConfig {
  @Bean
  public PollerConfig pollerConfig() {
    return org.mockito.Mockito.mock(PollerConfig.class);
  }

  @Bean
  public CustomPeriodicTrigger customPeriodicTrigger() {
    return new CustomPeriodicTrigger();
  }
}
