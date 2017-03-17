package uk.gov.ons.ctp.response.kirona.drs.message.impl;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Publisher;
import uk.gov.ons.ctp.response.action.message.instruction.*;
import uk.gov.ons.ctp.response.kirona.drs.message.InstructionPublisher;

/**
 * The service that puts ActionInstructions on the inbound channel. This is used for testing purposes only.
 */
@MessageEndpoint
public class InstructionPublisherImpl implements InstructionPublisher {

  /**
   * To put an ActionInstruction on the inbound channel.
   * @param actionInstruction the actionInstruction to deal with
   * @return the actionInstruction to put on the queue
   */
  @Override
  @Publisher(channel = "instructionOutbound")
  public ActionInstruction sendInstructions(ActionInstruction actionInstruction) {
    return actionInstruction;
  }
}
