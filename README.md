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
- I started the app at 11h28

- I put 100 ActionInstructions containing 10 ActionRequests each on the queue Action.Field with:
curl -H "Accept: application/json" -H "X-B3-TraceId: 73b62c0f90d11e01" -H "X-B3-SpanId: 73b62c0f90d11e10" http://localhost:8131/kironaTest/loadqueue/100/ai/10/ar/valid/true -v -X POST
201 and check the queue Action.Field for 100 messages

- ActionInstructions are processed before 11h31 and UNFORTUNATELY after 11h31 despite CustomPeriodicTrigger realising that it should not do anything until 18:00:01 UTC 2017. See the log extract below:
2017-03-17 11:30:59.996 DEBUG [projectTotestTaskExecutorPauses,,,]  15742 --- [ask-scheduler-8] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : earliestActiveDate is null
2017-03-17 11:30:59.996 DEBUG [projectTotestTaskExecutorPauses,,,]  15742 --- [ask-scheduler-8] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : result is Fri Mar 17 11:31:00 UTC 2017
2017-03-17 11:31:00.099 DEBUG [projectTotestTaskExecutorPauses,,,]  15742 --- [ask-scheduler-8] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : earliestActiveDate is Fri Mar 17 18:00:00 UTC 2017
2017-03-17 11:31:00.099 DEBUG [projectTotestTaskExecutorPauses,,,]  15742 --- [ask-scheduler-8] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : result is Fri Mar 17 18:00:01 UTC 2017
2017-03-17 11:31:54.488 DEBUG [projectTotestTaskExecutorPauses,,,]  15742 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : Exiting simulateProcessActionRequestsPausingThread...
2017-03-17 11:31:54.488 DEBUG [projectTotestTaskExecutorPauses,,,]  15742 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : It took 60001 milliseconds to process the instruction.
2017-03-17 11:31:54.492 DEBUG [projectTotestTaskExecutorPauses,,,]  15742 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : Entering process for instruction uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction@47ce9dc5
2017-03-17 11:31:54.492 DEBUG [projectTotestTaskExecutorPauses,,,]  15742 --- [mTaskExecutor-1] .r.k.d.m.i.ActionInstructionReceiverImpl : Entering simulateProcessActionRequestsPausingThread..


##################################################
# Scenario where the poller does work correctly
##################################################
- I start the app at 11h39 (ie after the pausing period start of 11h31).
- I put 100 messages on queue
- nothing gets processed. The log extract I get is:
2017-03-17 11:39:56.522 DEBUG [projectTotestTaskExecutorPauses,,,]  16490 --- [           main] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : earliestActiveDate is Fri Mar 17 18:00:56 UTC 2017
2017-03-17 11:39:56.524 DEBUG [projectTotestTaskExecutorPauses,,,]  16490 --- [           main] u.g.o.c.r.k.d.u.CustomPeriodicTrigger    : result is Fri Mar 17 18:00:57 UTC 2017
2017-03-17 11:39:56.700  INFO [projectTotestTaskExecutorPauses,,,]  16490 --- [           main] u.g.o.c.r.kirona.drs.GatewayApplication  : Started GatewayApplication in 18.224 seconds (JVM running for 19.269)
