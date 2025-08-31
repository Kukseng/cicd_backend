FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy Gradle wrapper and files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x ./gradlew

# Build the application (skip tests to avoid Redis connection)
RUN ./gradlew bootJar --no-daemon -x test

# Run the JAR
ENTRYPOINT ["java", "-jar", "build/libs/stackquiz-api-0.0.1-SNAPSHOT.jar"]