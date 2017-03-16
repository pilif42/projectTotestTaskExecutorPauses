package uk.gov.ons.ctp.response.kirona.drs.message.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Publisher;
import uk.gov.ons.ctp.response.action.message.instruction.*;
import uk.gov.ons.ctp.response.kirona.drs.message.InstructionPublisher;

/**
 * The service that puts ActionInstructions on the inbound channel. This is used for testing purposes only.
 */
@MessageEndpoint
public class InstructionPublisherImpl implements InstructionPublisher {

  private static final String SEND_INSTRUCTION = "SendingInstruction";

  @Autowired
  private Tracer tracer;

  /**
   * To put an ActionInstruction on the inbound channel.
   * @param actionInstruction the actionInstruction to deal with
   * @return the actionInstruction to put on the queue
   */
  @Override
  @Publisher(channel = "instructionOutbound")
  public ActionInstruction sendInstructions(ActionInstruction actionInstruction) {
    Span span = tracer.createSpan(SEND_INSTRUCTION);
    tracer.close(span);
    return actionInstruction;
  }
}
