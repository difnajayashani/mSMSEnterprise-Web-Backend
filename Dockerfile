#FROM openjdk:17
FROM amazoncorretto:22-alpine3.17-jdk
WORKDIR /
RUN mkdir -p /Apps/mSMSEnterpriseWebBEAPI
ARG JAR_FILE=*.jar
ADD ${JAR_FILE} msmsenterprisewebbeapi.jar
EXPOSE 80
ENTRYPOINT ["java","-jar","msmsenterprisewebbeapi.jar","--server.servlet.context-path=/","--server.port=80"]