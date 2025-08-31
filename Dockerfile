# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set work directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the project
RUN ./gradlew bootJar --no-daemon

# Expose port
EXPOSE 9999

# Run the application
ENTRYPOINT ["java", "-jar", "build/libs/stackquiz-api.jar"]
