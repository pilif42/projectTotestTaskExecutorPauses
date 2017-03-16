package uk.gov.ons.ctp.response.kirona.drs.utility;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.response.kirona.drs.config.PollerConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

  private Method calculateBufferedStartBoundaryMethod;
  private Method provideEarliestActiveDateMethod;

  private static final int BUFFER_KEEPING_SUPPORT_START_IN_SAME_HOUR = 30;

  private static final String SUPPORT_HOURS_START_ONE = "0.31";
  private static final String SUPPORT_HOURS_END_ONE = "8.30";
  private static final String SUPPORT_HOURS_START_TWO = "12.30";
  private static final String SUPPORT_HOURS_END_TWO = "13.30";
  private static final String SUPPORT_HOURS_START_THREE = "21.30";
  private static final String SUPPORT_HOURS_END_THREE = "23.59";

  @Before
  public void setup() throws NoSuchMethodException {
    when(pollerConfig.getSupportHoursStart()).thenReturn(buildSupportHoursStart());
    when(pollerConfig.getSupportHoursEnd()).thenReturn(buildSupportHoursEnd());
    when(pollerConfig.getSupportHoursBuffer()).thenReturn(BUFFER_KEEPING_SUPPORT_START_IN_SAME_HOUR);

    provideEarliestActiveDateMethod = CustomPeriodicTrigger.class.getDeclaredMethod("provideEarliestActiveDate", Date.class);
    provideEarliestActiveDateMethod.setAccessible(true);

    calculateBufferedStartBoundaryMethod = CustomPeriodicTrigger.class.getDeclaredMethod("calculateBufferedStartBoundary", String.class);
    calculateBufferedStartBoundaryMethod.setAccessible(true);
  }

  @Test
  public void testCurrentHourBetweenZeroAndFirstSupportHoursBufferedStart() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 30);
    Date testedDate = calendar.getTime();


    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentHourEqualsFirstSupportHoursBufferedStart() throws IllegalAccessException, InvocationTargetException {
    double bufferedStart = (Double)calculateBufferedStartBoundaryMethod.invoke(customPeriodicTrigger, SUPPORT_HOURS_START_ONE);

    Calendar calendar = Calendar.getInstance();
    int nbOfHours = (int)bufferedStart;
    int nbOfMinutes = (int)(100 * (bufferedStart - nbOfHours));
    calendar.set(Calendar.HOUR_OF_DAY, nbOfHours);
    calendar.set(Calendar.MINUTE, nbOfMinutes);
    Date testedDate = calendar.getTime();

    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNotNull(activeDate);

    int commaPosition = SUPPORT_HOURS_END_ONE.indexOf('.');
    int endBoundaryHour = new Integer(SUPPORT_HOURS_END_ONE.substring(0, commaPosition));
    int endBoundaryMinute = new Integer(SUPPORT_HOURS_END_ONE.substring(commaPosition + 1, SUPPORT_HOURS_END_ONE.length()));

    calendar.setTime(activeDate);
    assertEquals(endBoundaryHour, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(endBoundaryMinute, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentHourEqualsFirstSupportHoursStart() throws IllegalAccessException, InvocationTargetException {
    int commaPosition = SUPPORT_HOURS_START_ONE.indexOf('.');
    int startBoundaryHour = new Integer(SUPPORT_HOURS_START_ONE.substring(0, commaPosition));
    int startBoundaryMinute = new Integer(SUPPORT_HOURS_START_ONE.substring(commaPosition + 1, SUPPORT_HOURS_START_ONE.length()));

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, startBoundaryHour);
    calendar.set(Calendar.MINUTE, startBoundaryMinute);
    Date testedDate = calendar.getTime();

    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNotNull(activeDate);

    commaPosition = SUPPORT_HOURS_END_ONE.indexOf('.');
    int endBoundaryHour = new Integer(SUPPORT_HOURS_END_ONE.substring(0, commaPosition));
    int endBoundaryMinute = new Integer(SUPPORT_HOURS_END_ONE.substring(commaPosition + 1, SUPPORT_HOURS_END_ONE.length()));

    calendar.setTime(activeDate);
    assertEquals(endBoundaryHour, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(endBoundaryMinute, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentHourEqualsFirstSupportHoursEnd() throws IllegalAccessException, InvocationTargetException {
    int commaPosition = SUPPORT_HOURS_END_ONE.indexOf('.');
    int endBoundaryHour = new Integer(SUPPORT_HOURS_END_ONE.substring(0, commaPosition));
    int endBoundaryMinute = new Integer(SUPPORT_HOURS_END_ONE.substring(commaPosition + 1, SUPPORT_HOURS_END_ONE.length()));

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, endBoundaryHour);
    calendar.set(Calendar.MINUTE, endBoundaryMinute);
    Date testedDate = calendar.getTime();

    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentHourBetweenFirstSupportHoursStartAndFirstSupportHoursEnd() throws IllegalAccessException, InvocationTargetException {
    int commaPosition = SUPPORT_HOURS_START_ONE.indexOf('.');
    int startBoundaryHour = new Integer(SUPPORT_HOURS_START_ONE.substring(0, commaPosition));
    int startBoundaryMinute = new Integer(SUPPORT_HOURS_START_ONE.substring(commaPosition + 1, SUPPORT_HOURS_START_ONE.length()));

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, startBoundaryHour);
    calendar.set(Calendar.MINUTE, startBoundaryMinute + 15);
    Date testedDate = calendar.getTime();

    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNotNull(activeDate);

    commaPosition = SUPPORT_HOURS_END_ONE.indexOf('.');
    int endBoundaryHour = new Integer(SUPPORT_HOURS_END_ONE.substring(0, commaPosition));
    int endBoundaryMinute = new Integer(SUPPORT_HOURS_END_ONE.substring(commaPosition + 1, SUPPORT_HOURS_END_ONE.length()));

    calendar.setTime(activeDate);
    assertEquals(endBoundaryHour, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(endBoundaryMinute, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentHourBetweenSecondSupportHoursStartAndSecondSupportHoursEnd() throws IllegalAccessException, InvocationTargetException {
    int commaPosition = SUPPORT_HOURS_START_TWO.indexOf('.');
    int startBoundaryHour = new Integer(SUPPORT_HOURS_START_TWO.substring(0, commaPosition));
    int startBoundaryMinute = new Integer(SUPPORT_HOURS_START_TWO.substring(commaPosition + 1, SUPPORT_HOURS_START_TWO.length()));

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, startBoundaryHour);
    calendar.set(Calendar.MINUTE, startBoundaryMinute + 15);
    Date testedDate = calendar.getTime();

    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNotNull(activeDate);

    commaPosition = SUPPORT_HOURS_END_TWO.indexOf('.');
    int endBoundaryHour = new Integer(SUPPORT_HOURS_END_TWO.substring(0, commaPosition));
    int endBoundaryMinute = new Integer(SUPPORT_HOURS_END_TWO.substring(commaPosition + 1, SUPPORT_HOURS_END_TWO.length()));

    calendar.setTime(activeDate);
    assertEquals(endBoundaryHour, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(endBoundaryMinute, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentHourEqualsLastSupportHoursStart() throws IllegalAccessException, InvocationTargetException {
    int commaPosition = SUPPORT_HOURS_START_THREE.indexOf('.');
    int startBoundaryHour = new Integer(SUPPORT_HOURS_START_THREE.substring(0, commaPosition));
    int startBoundaryMinute = new Integer(SUPPORT_HOURS_START_THREE.substring(commaPosition + 1, SUPPORT_HOURS_START_THREE.length()));

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, startBoundaryHour);
    calendar.set(Calendar.MINUTE, startBoundaryMinute);
    Date testedDate = calendar.getTime();

    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNotNull(activeDate);

    commaPosition = SUPPORT_HOURS_END_THREE.indexOf('.');
    int endBoundaryHour = new Integer(SUPPORT_HOURS_END_THREE.substring(0, commaPosition));
    int endBoundaryMinute = new Integer(SUPPORT_HOURS_END_THREE.substring(commaPosition + 1, SUPPORT_HOURS_END_THREE.length()));

    calendar.setTime(activeDate);
    assertEquals(endBoundaryHour, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(endBoundaryMinute, calendar.get(Calendar.MINUTE));
  }

  @Test
  public void testCurrentHourEqualsLastSupportHoursEnd() throws IllegalAccessException, InvocationTargetException {
    int commaPosition = SUPPORT_HOURS_END_THREE.indexOf('.');
    int endBoundaryHour = new Integer(SUPPORT_HOURS_END_THREE.substring(0, commaPosition));
    int endBoundaryMinute = new Integer(SUPPORT_HOURS_END_THREE.substring(commaPosition + 1, SUPPORT_HOURS_END_THREE.length()));

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, endBoundaryHour);
    calendar.set(Calendar.MINUTE, endBoundaryMinute);
    Date testedDate = calendar.getTime();

    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNull(activeDate);
  }

  @Test
  public void testCurrentHourBetweenLastSupportHoursEndAnd2359() throws IllegalAccessException, InvocationTargetException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 30);
    Date testedDate = calendar.getTime();

    Date activeDate = (Date)provideEarliestActiveDateMethod.invoke(customPeriodicTrigger, testedDate);
    assertNull(activeDate);
  }

  private static List<String> buildSupportHoursStart() {
    List<String> supportHoursStart = new ArrayList();
    supportHoursStart.add(SUPPORT_HOURS_START_ONE);
    supportHoursStart.add(SUPPORT_HOURS_START_TWO);
    supportHoursStart.add(SUPPORT_HOURS_START_THREE);
    return supportHoursStart;
  }

  private static List<String> buildSupportHoursEnd() {
    List<String> supportHoursEnd = new ArrayList();
    supportHoursEnd.add(SUPPORT_HOURS_END_ONE);
    supportHoursEnd.add(SUPPORT_HOURS_END_TWO);
    supportHoursEnd.add(SUPPORT_HOURS_END_THREE);
    return supportHoursEnd;
  }

}
