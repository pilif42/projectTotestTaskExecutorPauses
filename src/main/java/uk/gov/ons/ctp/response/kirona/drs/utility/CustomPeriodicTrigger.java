package uk.gov.ons.ctp.response.kirona.drs.utility;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import uk.gov.ons.ctp.response.kirona.drs.config.PollerConfig;

import java.util.Calendar;
import java.util.Date;

/**
 * A trigger to meet Kirona DRS needs
 */
@Slf4j
@Data
public class CustomPeriodicTrigger implements Trigger {

    @Autowired
    private PollerConfig pollerConfig;

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date currentDate = new Date(System.currentTimeMillis());
        Date earliestActiveDate = provideEarliestActiveDate(currentDate);
        log.debug("earliestActiveDate is {}", earliestActiveDate);

        Date result = null;
        if (earliestActiveDate != null) {
            result = new Date(earliestActiveDate.getTime() + pollerConfig.getInitialDelay());
        } else {
            if (triggerContext.lastScheduledExecutionTime() == null) {
                result = new Date(System.currentTimeMillis() + pollerConfig.getInitialDelay());
            } else {
                if (pollerConfig.isFixedRate()) {
                    result = new Date(triggerContext.lastScheduledExecutionTime().getTime() +
                            pollerConfig.getFixedDelay());
                } else {
                    result = new Date(triggerContext.lastCompletionTime().getTime() +
                            pollerConfig.getFixedDelay());
                }
            }
        }

        log.debug("result is {}", result);
        return result;
    }

    /**
     * This returns null if the current date is already active, ie outside of support hours.
     * If the current date is within support hours, it returns the earliest active date.
     *
     * @param date the current date
     * @return aDate
     */
    public Date provideEarliestActiveDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dateHours = calendar.get(Calendar.HOUR_OF_DAY);

        int supportHourStart = pollerConfig.getSupportHourStart();
        int supportHourEnd = pollerConfig.getSupportHourEnd();

        if (dateHours < supportHourStart || dateHours >= supportHourEnd) {
            return null;
        } else {
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, supportHourEnd);
            calendar.set(Calendar.MINUTE, 0);
            return calendar.getTime();
        }
    }
}
