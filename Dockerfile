# Dockerfile from https://medium.com/@ramanamuttana/build-a-docker-image-using-maven-and-spring-boot-418e24c00776

# ----- First stage: Build -----
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ----- Second stage: Run -----
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update && apt-get install -y sqlite3

COPY --from=builder /app/target/*.jar app.jar

COPY database_schema.sql .

RUN sqlite3 database.sqlite < database_schema.sql

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]