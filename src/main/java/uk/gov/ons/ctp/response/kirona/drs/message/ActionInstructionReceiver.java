package uk.gov.ons.ctp.response.kirona.drs.message;

import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;

/**
 * The service that reads ActionInstructions from the inbound channel
 */
public interface ActionInstructionReceiver {
    /**
     * To process ActionInstructions from the input channel actionInstructionTransformed
     * @param instruction the ActionInstruction to be processed
     */
    void processInstruction(ActionInstruction instruction);
}
