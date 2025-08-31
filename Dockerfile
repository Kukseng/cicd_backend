FROM openjdk:17-jdk-slim AS builder
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build with build profile (skip tests that require external services)
RUN ./gradlew bootJar --no-daemon -Dspring.profiles.active=build -x test

# Runtime stage
FROM openjdk:17-jre-slim
WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Run the application (will use default profile with external Redis)
ENTRYPOINT ["java", "-jar", "app.jar"]