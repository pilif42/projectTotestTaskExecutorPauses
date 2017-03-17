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

  private static final int SUPPORT_HOUR_START = 16;
  private static final int SUPPORT_MINUTE_START = 20;
  private static final int SUPPORT_HOUR_END = 18;

  @Before
  public void setup() throws NoSuchMethodException {
    when(pollerConfig.getSupportHourStart()).thenReturn(SUPPORT_HOUR_START);
    when(pollerConfig.getSupportMinuteStart()).thenReturn(SUPPORT_MINUTE_START);
    when(pollerConfig.getSupportHourEnd()).thenReturn(SUPPORT_HOUR_END);
  }

  @Test
  public void testCurrentDateBetweenZeroAndSupportStartScenario1() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 15);
    calendar.set(Calendar.MINUTE, 10);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentDateBetweenZeroAndSupportStartScenario2() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 16);
    calendar.set(Calendar.MINUTE, 10);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentDateBetweenSupportStartAndEndScenario1() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 16);
    calendar.set(Calendar.MINUTE, 30);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNotNull(activeDate);

    calendar.setTime(activeDate);
    assertEquals(pollerConfig.getSupportHourEnd(), calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentDateBetweenSupportStartAndEndScenario2() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 17);
    calendar.set(Calendar.MINUTE, 30);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNotNull(activeDate);

    calendar.setTime(activeDate);
    assertEquals(pollerConfig.getSupportHourEnd(), calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentDateAfterSupportEndScenario1() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 18);
    calendar.set(Calendar.MINUTE, 10);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentDateAfterSupportEndScenario2() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 19);
    calendar.set(Calendar.MINUTE, 10);
    Date testedDate = calendar.getTime();

    Date activeDate = customPeriodicTrigger.provideEarliestActiveDate(testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentDateEqualsSupportStart() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 16);
    calendar.set(Calendar.MINUTE, 20);
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
