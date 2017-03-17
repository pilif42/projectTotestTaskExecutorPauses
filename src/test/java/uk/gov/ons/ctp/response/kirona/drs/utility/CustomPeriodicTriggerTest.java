package uk.gov.ons.ctp.response.kirona.drs.utility;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.response.kirona.drs.config.PollerConfig;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CustomPeriodicTriggerTestConfig.class)
public class CustomPeriodicTriggerTest {

  @Autowired
  private PollerConfig pollerConfig;

  @Autowired
  private CustomPeriodicTrigger customPeriodicTrigger;

  private static final int SUPPORT_HOUR_START = 17;
  private static final int SUPPORT_MINUTE_START = 50;
  private static final int SUPPORT_HOUR_END = 18;

  @Before
  public void setup() throws NoSuchMethodException {
    when(pollerConfig.getSupportHourStart()).thenReturn(SUPPORT_HOUR_START);
    when(pollerConfig.getSupportMinuteStart()).thenReturn(SUPPORT_MINUTE_START);
    when(pollerConfig.getSupportHourEnd()).thenReturn(SUPPORT_HOUR_END);
  }

  @Test
  public void testCurrentDateBetweenZeroAndSupportStart() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 17);
    calendar.set(Calendar.MINUTE, 40);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentDateBetweenSupportStartAndEnd() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 17);
    calendar.set(Calendar.MINUTE, 55);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNotNull(activeDate);

    calendar.setTime(activeDate);
    assertEquals(pollerConfig.getSupportHourEnd(), calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentDateAfterSupportEnd() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 18);
    calendar.set(Calendar.MINUTE, 10);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentDateEqualsSupportStart() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 17);
    calendar.set(Calendar.MINUTE, 50);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNotNull(activeDate);

    calendar.setTime(activeDate);
    assertEquals(pollerConfig.getSupportHourEnd(), calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentDateEqualsSupportEnd() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 18);
    calendar.set(Calendar.MINUTE, 0);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNull(activeDate);
  }
}
