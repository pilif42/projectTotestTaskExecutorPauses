package uk.gov.ons.ctp.response.kirona.drs.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.action.message.instruction.ActionAddress;
import uk.gov.ons.ctp.response.action.message.instruction.ActionEvent;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequests;
import uk.gov.ons.ctp.response.action.message.instruction.Priority;
import uk.gov.ons.ctp.response.kirona.drs.message.ActionInstructionReceiver;
import uk.gov.ons.ctp.response.kirona.drs.message.InstructionPublisher;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller that provides testing endpoints to verify connectivity to Kirona and to create an order in Kirona DRS.
 */
@RestController
@Slf4j
public class TestingEndpoint {

  @Autowired
  private ActionInstructionReceiver actionInstructionReceiver;

  @Autowired
  private InstructionPublisher instructionPublisher;

  private static final String ACTION_PLAN = "INDIACL";
  private static final Integer CASE_ID = new Integer("1");
  private static final String CASE_REF = "1";
  private static final String IAC = "123456789";
  private static final String QUESTION_SET = "HS1";
  private static final String STRING = "string";

  private static final BigInteger BLACKPOOL_UPRN = new BigInteger("2");
  private static final String BLACKPOOL_LINE2 = "FINCHLEY ROAD";
  private static final String BLACKPOOL_TOWNNAME = "BLACKPOOL";
  private static final String BLACKPOOL_POSTCODE = "FY1 2LP";
  private static final BigDecimal BLACKPOOL_LATITUDE = BigDecimal.valueOf(53.8323247);
  private static final BigDecimal BLACKPOOL_LONGITUDE = BigDecimal.valueOf(-3.0525692);

  /**
   * Endpoint to put ActionInstructions on the Action.Field queue. This enables us to load test without the need to
   * involve the CSVIngester from ActionSvc. We provide the number of ActionInstructions and the number of
   * ActionRequests inside each ActionInstruction.
   *
   * @param nbAIs the number of ActionInstructions
   * @param nbARs the number of ActionRequests inside each ActionInstruction
   * @param validAI true if we produce valid ActionInstructions
   * @return 201 when ActionInstructions are published successfully.
   */
  @RequestMapping(value = "/kironaTest/loadqueue/{nbAIs}/ai/{nbARs}/ar/valid/{validAI}", method = RequestMethod.POST)
  public final ResponseEntity<?> loadingActionFieldQueue(@PathVariable(value = "nbAIs") final int nbAIs,
                                                         @PathVariable(value = "nbARs") final int nbARs,
                                                         @PathVariable(value = "validAI") final boolean validAI) {
    log.debug("Entering loadingActionFieldQueue ...");
    for (int i = 0; i < nbAIs; i++) {
      instructionPublisher.sendInstructions(buildActionInstruction(nbARs, "HOUSEHOLD", buildBlackpoolAddress(validAI)));
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  private ActionInstruction buildActionInstruction(int numberOfActionRequests, String actionType,
                                                   ActionAddress actionAddress) {
    ActionInstruction actionInstruction = new ActionInstruction();

    ActionRequest[] actionRequestArray = new ActionRequest[numberOfActionRequests];
    for (int i = 0; i < numberOfActionRequests; i++) {
      actionRequestArray[i] = buildActionRequest(BigInteger.valueOf(i + 1), actionType, actionAddress);
    }
    actionInstruction.setActionRequests(buildActionRequests(actionRequestArray));
    return actionInstruction;
  }

  private ActionRequests buildActionRequests(ActionRequest... actionRequestObjects) {
    ActionRequests actionRequests = new ActionRequests();
    List<ActionRequest> actionRequestList = new ArrayList<>();
    if (actionRequestObjects != null) {
      for (int i = 0; i < actionRequestObjects.length; i++) {
        actionRequestList.add(actionRequestObjects[i]);
      }
    }
    actionRequests.getActionRequests().addAll(actionRequestList);
    return actionRequests;
  }

  private ActionRequest buildActionRequest(BigInteger actionId, String actionType, ActionAddress actionAddress) {
    ActionRequest actionRequest = new ActionRequest();
    actionRequest.setActionId(actionId);
    actionRequest.setActionType(actionType);
    actionRequest.setAddress(actionAddress);
    actionRequest.setCaseId(CASE_ID);
    List<String> eventsList = new ArrayList<>();
    eventsList.add("IWantToTestTheLimitAt100WeThoughtItWas250code");
    ActionEvent actionEvent = new ActionEvent();
    actionEvent.getEvents().addAll(eventsList);
    actionRequest.setEvents(actionEvent);

    actionRequest.setIac(IAC);
    actionRequest.setPriority(Priority.HIGHEST);
    actionRequest.setCaseRef(CASE_REF);
    actionRequest.setQuestionSet(QUESTION_SET);
    actionRequest.setActionPlan(ACTION_PLAN);

    actionRequest.setResponseRequired(true);

    return actionRequest;
  }

  private ActionAddress buildActionAddress(BigInteger uprn, String line2, String townName, String postcode,
                                           String ladCode, BigDecimal latitude, BigDecimal longitude) {
    ActionAddress actionAddress = new ActionAddress();
    actionAddress.setLine1("");
    actionAddress.setLine2(line2);
    actionAddress.setLocality(STRING);
    actionAddress.setTownName(townName);
    actionAddress.setPostcode(postcode);
    actionAddress.setCategory("Household");
    actionAddress.setUprn(uprn);
    actionAddress.setType(STRING);
    actionAddress.setEstabType(STRING);
    actionAddress.setOrganisationName(STRING);
    actionAddress.setLatitude(latitude);
    actionAddress.setLongitude(longitude);
    actionAddress.setLadCode(ladCode);
    return actionAddress;
  }

  private ActionAddress buildBlackpoolAddress(boolean valid) {
    if (valid) {
      return buildActionAddress(BLACKPOOL_UPRN, BLACKPOOL_LINE2, BLACKPOOL_TOWNNAME, BLACKPOOL_POSTCODE,
              "Blackpool", BLACKPOOL_LATITUDE, BLACKPOOL_LONGITUDE);
    } else {
      // Note the 0 below. This will fail vs the .xsd as UPRN is expected to be a xs:positiveInteger
      return buildActionAddress(new BigInteger("0"), BLACKPOOL_LINE2, BLACKPOOL_TOWNNAME, BLACKPOOL_POSTCODE,
              "Blackpool", BLACKPOOL_LATITUDE, BLACKPOOL_LONGITUDE);
    }
  }

}
