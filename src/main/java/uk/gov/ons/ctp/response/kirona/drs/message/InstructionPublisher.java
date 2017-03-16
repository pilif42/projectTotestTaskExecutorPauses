package uk.gov.ons.ctp.response.kirona.drs.message;

import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;

/**
 * The service that puts ActionInstructions on the inbound channel. This is used for testing purposes only.
 */
public interface InstructionPublisher {

  /**
   * @param actionInstruction the actionInstruction to deal with
   * @return the ActionInstruction
   */
  ActionInstruction sendInstructions(ActionInstruction actionInstruction);
}
