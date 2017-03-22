############
# To build
############
mvn clean install


##################
# To run the app
##################
# Prerequisites
    - Install and configure RabbitMQ (I used v3.6.6)
            - Start RabbitMQ: sudo /sbin/service rabbitmq-server start
            - Enable the console with: rabbitmq-plugins enable rabbitmq_management
            - Check the RabbitMQ console at http://localhost:15672/ with guest / guest

java -jar target/taskExecutorPauses-1.0.0-SNAPSHOT.jar


##################################################
# Notes on the poller configuration
##################################################
As it stands (see application.yml and the poller section), we have defined 1 period during which we want to stop the processing (ie processInstruction in ActionInstructionReceiverImpl is NOT invoked)
    - 11h31 to 18h


##################################################
# To replicate the issue
##################################################
- I started the app at 16h17

- I put 100 ActionInstructions containing 10 ActionRequests each on the queue Action.Field with:
curl -H "Accept: application/json" -H "X-B3-TraceId: 73b62c0f90d11e01" -H "X-B3-SpanId: 73b62c0f90d11e10" http://localhost:8131/kironaTest/loadqueue/1000/ai/10/ar/valid/true -v -X POST
201 and check the queue Action.Field for 100 messages

- ActionInstructions are processed before 16h20 and UNFORTUNATELY after 16h20 despite CustomPeriodicTrigger realising that it should not do anything until 18:00:01 UTC 2017. See the log extract below:
result is Fri Mar 17 16:20:00 UTC 2017
2017-03-17 16:20:00.023 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [ask-scheduler-3] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : earliestActiveDate is Fri Mar 17 18:00:00 UTC 2017
2017-03-17 16:20:00.023 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [ask-scheduler-3] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : result is Fri Mar 17 18:00:01 UTC 2017
2017-03-17 16:20:00.814 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Initiating transaction commit
2017-03-17 16:20:00.815 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Creating new transaction with name [actionInstructionMessageListenerContainer]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2017-03-17 16:20:01.833 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Initiating transaction commit
2017-03-17 16:20:01.834 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Creating new transaction with name [actionInstructionMessageListenerContainer]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2017-03-17 16:20:02.725 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : Exiting simulateProcessActionRequestsPausingThread...
2017-03-17 16:20:02.726 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : It took 60001 milliseconds to process the instruction.
2017-03-17 16:20:02.726 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] o.s.i.handler.ServiceActivatingHandler   : handler 'ServiceActivator for [org.springframework.integration.handler.MethodInvokingMessageProcessor@60ac5353] (actionInstructionReceiverImpl.processInstruction.serviceActivator.handler)' produced no reply for request Message: GenericMessage [payload=uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction@70148a47, headers={jms_destination=RMQDestination{destinationName='Action.Field', queue(permanent)', amqpExchangeName='jms.durable.queues', amqpRoutingKey='Action.Field', amqpQueueName='Action.Field'}, X-B3-ParentSpanId=fe94794fed5b5f21, X-Message-Sent=true, messageSent=true, priority=4, jms_timestamp=1489750134410, spanName=message:actionInstructionTransformed, spanTraceId=73b62c0f90d11e01, spanId=51ad695384b18aff, spanParentSpanId=fe94794fed5b5f21, jms_redelivered=false, X-Span-Name=message:actionInstructionTransformed, X-B3-SpanId=51ad695384b18aff, X-B3-Sampled=1, X-B3-TraceId=73b62c0f90d11e01, id=c6bb8864-7b58-64f5-1845-f2a8a7462690, spanSampled=1, jms_messageId=ID:22e9d9e9-0d2b-450c-8cbe-9214f00b48bb, timestamp=1489750135643}]
2017-03-17 16:20:02.726 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] o.s.i.t.PseudoTransactionManager         : Initiating transaction commit
2017-03-17 16:20:02.727 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] o.s.i.t.PseudoTransactionManager         : Creating new transaction with name [org.springframework.integration.endpoint.AbstractPollingEndpoint$1.call]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly
2017-03-17 16:20:02.727 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] o.s.integration.jms.DynamicJmsTemplate   : Executing callback on JMS Session: Cached JMS Session: com.rabbitmq.jms.client.RMQSession@17f3de06
2017-03-17 16:20:02.732 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] o.s.integration.jms.PollableJmsChannel   : postReceive on channel 'actionInstructionTransformed', message: GenericMessage [payload=uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction@32599850, headers={jms_destination=RMQDestination{destinationName='Action.Field', queue(permanent)', amqpExchangeName='jms.durable.queues', amqpRoutingKey='Action.Field', amqpQueueName='Action.Field'}, X-B3-ParentSpanId=d651dd1f3baf40ef, X-Message-Sent=true, messageSent=true, priority=4, jms_timestamp=1489750134436, spanName=message:actionInstructionTransformed, spanTraceId=73b62c0f90d11e01, spanId=78521c3124a2ba0, spanParentSpanId=d651dd1f3baf40ef, jms_redelivered=false, X-Span-Name=message:actionInstructionTransformed, X-B3-SpanId=78521c3124a2ba0, X-B3-Sampled=1, X-B3-TraceId=73b62c0f90d11e01, id=439e33d1-3dd8-a9b0-2e3d-0938277d0136, spanSampled=1, jms_messageId=ID:ac46f279-0014-49ab-8350-7d42650076ff, timestamp=1489750135713}]
2017-03-17 16:20:02.732 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] o.s.i.endpoint.PollingConsumer           : Poll resulted in Message: GenericMessage [payload=uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction@32599850, headers={jms_destination=RMQDestination{destinationName='Action.Field', queue(permanent)', amqpExchangeName='jms.durable.queues', amqpRoutingKey='Action.Field', amqpQueueName='Action.Field'}, X-B3-ParentSpanId=d651dd1f3baf40ef, X-Message-Sent=true, messageSent=true, priority=4, jms_timestamp=1489750134436, spanName=message:actionInstructionTransformed, spanTraceId=73b62c0f90d11e01, spanId=78521c3124a2ba0, spanParentSpanId=d651dd1f3baf40ef, jms_redelivered=false, X-Span-Name=message:actionInstructionTransformed, X-B3-SpanId=78521c3124a2ba0, X-B3-Sampled=1, X-B3-TraceId=73b62c0f90d11e01, id=439e33d1-3dd8-a9b0-2e3d-0938277d0136, spanSampled=1, jms_messageId=ID:ac46f279-0014-49ab-8350-7d42650076ff, timestamp=1489750135713}]
2017-03-17 16:20:02.732 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] o.s.i.handler.ServiceActivatingHandler   : ServiceActivator for [org.springframework.integration.handler.MethodInvokingMessageProcessor@60ac5353] (actionInstructionReceiverImpl.processInstruction.serviceActivator.handler) received message: GenericMessage [payload=uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction@32599850, headers={jms_destination=RMQDestination{destinationName='Action.Field', queue(permanent)', amqpExchangeName='jms.durable.queues', amqpRoutingKey='Action.Field', amqpQueueName='Action.Field'}, X-B3-ParentSpanId=d651dd1f3baf40ef, X-Message-Sent=true, messageSent=true, priority=4, jms_timestamp=1489750134436, spanName=message:actionInstructionTransformed, spanTraceId=73b62c0f90d11e01, spanId=78521c3124a2ba0, spanParentSpanId=d651dd1f3baf40ef, jms_redelivered=false, X-Span-Name=message:actionInstructionTransformed, X-B3-SpanId=78521c3124a2ba0, X-B3-Sampled=1, X-B3-TraceId=73b62c0f90d11e01, id=439e33d1-3dd8-a9b0-2e3d-0938277d0136, spanSampled=1, jms_messageId=ID:ac46f279-0014-49ab-8350-7d42650076ff, timestamp=1489750135713}]
2017-03-17 16:20:15.262 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Initiating transaction commit
2017-03-17 16:20:15.867 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : Entering process for instruction uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction@32599850
2017-03-17 16:20:15.869 DEBUG [projectTotestTaskExecutorPauses,,,]  6922 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Creating new transacti


##################################################
# Scenario where the poller does work correctly
##################################################
- I start the app at 16h22 (ie after the pausing period start of 16h20).
- I put 100 messages on queue
- nothing gets processed. The log extract I get is:
ontainer-1] o.s.i.t.PseudoTransactionManager         : Creating new transaction with name [actionInstructionMessageListenerContainer]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2017-03-17 16:22:06.939 DEBUG [projectTotestTaskExecutorPauses,,,]  7243 --- [           main] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : earliestActiveDate is Fri Mar 17 18:00:06 UTC 2017
2017-03-17 16:22:06.941 DEBUG [projectTotestTaskExecutorPauses,,,]  7243 --- [           main] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : result is Fri Mar 17 18:00:07 UTC 2017
2017-03-17 16:22:06.943  INFO [projectTotestTaskExecutorPauses,,,]  7243 --- [           main] o.s.i.endpoint.PollingConsumer           : started actionInstructionReceiverImpl.processInstruction.serviceActivator
2017-03-17 16:22:07.054  INFO [projectTotestTaskExecutorPauses,,,]  7243 --- [           main] u.g.o.c.r.kirona.drs.GatewayApplication  : Started GatewayApplication in 17.827 seconds (JVM running for 19.31)
2017-03-17 16:22:08.249 DEBUG [projectTotestTaskExecutorPauses,,,]  7243 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Initiating transaction commit
2017-03-17 16:22:08.252 DEBUG [projectTotestTaskExecutorPauses,,,]  7243 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Creating new transaction with name [actionInstructionMessageListenerContainer]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2017-03-17 16:22:09.277 DEBUG [projectTotestTaskExecutorPauses,,,]  7243 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Initiating transaction commit
2017-03-17 16:22:09.278 DEBUG [projectTotestTaskExecutorPauses,,,]  7243 --- [enerContainer-1] o.s.i.t.PseudoTransactionManager         : Creating new transaction with name [actionInstructionMessageListenerContainer]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2017-03-17 16:22:10.297 DEBUG [projectTotestTaskExecutorPauses,,,]  7243 --- [enerContainer-1] o.s.i.t.Pseud


Now working thanks to (in action-instruction-inbound-flow.xml):
    - <property name="queueCapacity" value="1" />
    - <property name="rejectedExecutionHandler" ref="callerRunsPolicy" />
