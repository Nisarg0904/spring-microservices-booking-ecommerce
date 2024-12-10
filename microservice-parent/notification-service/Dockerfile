#-------------------
# BUILD STAGE:
#-------------------

FROM gradle:8-jdk22-alpine AS builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build -x test

#-------------------
# PACKAGE STAGE:
#-------------------

FROM openjdk:22-jdk

RUN mkdir /app

# untill here every micro service can have same code for Dockerfile but next line will change ( the name of jar file)
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/notification-service.jar


EXPOSE 8093
# Change here as well !
ENTRYPOINT ["java","-jar","/app/notification-service.jar"]
