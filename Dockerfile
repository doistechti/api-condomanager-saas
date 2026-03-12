FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM tomcat:10.1-jdk17-temurin
WORKDIR /usr/local/tomcat

RUN rm -rf webapps/*
COPY --from=build /app/target/*.war webapps/ROOT.war

EXPOSE 8080
