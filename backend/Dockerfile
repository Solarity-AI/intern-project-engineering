# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh
EXPOSE 10000
ENV PORT=10000
ENTRYPOINT ["./entrypoint.sh"]
