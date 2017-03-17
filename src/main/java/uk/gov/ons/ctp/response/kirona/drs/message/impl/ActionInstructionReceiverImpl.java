package uk.gov.ons.ctp.response.kirona.drs.message.impl;

import java.util.List;

import org.springframework.integration.annotation.MessageEndpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequests;
import uk.gov.ons.ctp.response.kirona.drs.message.ActionInstructionReceiver;

/**
 * The service that reads ActionInstructions from the inbound channel
 */
@Slf4j
@MessageEndpoint
public class ActionInstructionReceiverImpl implements ActionInstructionReceiver {

  /**
   * To process ActionInstructions from the input channel actionInstructionTransformed
   *
   * @param instruction the ActionInstruction to be processed
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true, value = "transactionManager")
  @ServiceActivator(inputChannel = "actionInstructionTransformed", poller = @Poller(value = "customPoller"))
  public final void processInstruction(final ActionInstruction instruction) {
    long timeAtEntrance = System.currentTimeMillis();
    log.debug("Entering process for instruction {}", instruction);

    ActionRequests actionRequests = instruction.getActionRequests();
    if (actionRequests != null) {
      List<ActionRequest> initialActionRequestList = actionRequests.getActionRequests();
      if (initialActionRequestList != null && !initialActionRequestList.isEmpty()) {
        simulateProcessActionRequestsPausingThread(initialActionRequestList);
      }
    }

    long timeAtExit= System.currentTimeMillis();
    log.debug("It took {} milliseconds to process the instruction.", timeAtExit - timeAtEntrance);
  }


  /**
   * Method used while we can't test with DRS
   *
   * @param actionRequestList the list of validated action requests to deal with
   */
  private void simulateProcessActionRequestsPausingThread(List<ActionRequest> actionRequestList) {
    log.debug("Entering simulateProcessActionRequestsPausingThread...");
    try{
      Thread.sleep(60000); // ie 1 minute
    } catch (InterruptedException e) {
      log.error("Pb pausing thread - msg = {} - cause = {}", e.getMessage(), e.getCause());
    }
    log.debug("Exiting simulateProcessActionRequestsPausingThread...");
  }
}
