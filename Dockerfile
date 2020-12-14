FROM openjdk:11
MAINTAINER Amir Muminov
ADD /target/order-service-0.0.1-SNAPSHOT.jar order-service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "order-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 8083
