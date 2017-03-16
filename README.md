############
# To build
############
mvn clean install


##################
# To run the app
##################
# Prerequisites
    - Stop RabbitMQ: sudo /sbin/service rabbitmq-server stop
    - Start ActiveMQ:
            - cd /opt/apache-activemq-5.13.3/bin
            - ./activemq console
            - check the console at http://localhost:8161/

java -jar target/taskExecutorPauses-1.0.0-SNAPSHOT.jar


##################################################
# Notes on the poller configuration
##################################################
As it stands (see application.yml and the poller section), we have defined 3 periods during which we want to stop the processing (ie processInstruction in ActionInstructionReceiverImpl is NOT invoked)
    - 00h21 to 08h30
    - 13h28 to 14h20
    - 21h20 to 23h59


##################################################
# To replicate the issue
##################################################
- I started the app at 13h25

- I put 100 ActionInstructions containing 10 ActionRequests each on the queue Action.Field with:
curl -H "Accept: application/json" -H "X-B3-TraceId: 73b62c0f90d11e01" -H "X-B3-SpanId: 73b62c0f90d11e10" http://localhost:8131/kironaTest/loadqueue/100/ai/10/ar/valid/true -v -X POST
201 and check the queue Action.Field for 100 more messages

- ActionInstructions were processed before 13h28 and unfortunately after 13h28 despite CustomPeriodicTrigger realising that it should not do anything until 14:20. See the log extract below:
... result is Thu Mar 16 13:28:00 UTC 2017
2017-03-16 13:28:00.081 DEBUG [DRSGatewaySvc,,,]  24724 --- [ask-scheduler-8] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : earliestActiveDate is Thu Mar 16 14:20:00 UTC 2017
2017-03-16 13:28:00.081 DEBUG [DRSGatewaySvc,,,]  24724 --- [ask-scheduler-8] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : result is Thu Mar 16 14:20:01 UTC 2017
2017-03-16 13:28:11.123 DEBUG [DRSGatewaySvc,d386fec24551f33,d386fec24551f33,false]  24724 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : Exiting simulateProcessActionRequestsPausingThread...
2017-03-16 13:28:11.123 DEBUG [DRSGatewaySvc,,,]  24724 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : It took 60001 milliseconds to process the instruction.
2017-03-16 13:28:26.336 DEBUG [DRSGatewaySvc,,,]  24724 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : Entering process for instruction uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction@604ec860
