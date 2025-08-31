FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

# Use wildcard to match any JAR file
COPY build/libs/*.jar app.jar


ENTRYPOINT ["java", "-jar", "app.jar"]