# Stage 1: Build JAR using Maven
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Run Spring Boot application
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/JavaMailApp.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]