##########
# To build
##########
mvn clean install -P artifactory-aws


##################################################
# To run the app
##################################################
# Prerequisites
    - Install and configure RabbitMQ (I used v3.6.6)
            - Start RabbitMQ: sudo /sbin/service rabbitmq-server start
            - Enable the console with: rabbitmq-plugins enable rabbitmq_management
            - Check the RabbitMQ console at http://localhost:15672/ with guest / guest

java -jar target/taskExecutorPauses-1.0.0-SNAPSHOT.jar


##################################################
# To test
##################################################
See curlTests.txt under /resources
