FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy Gradle wrapper and files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x ./gradlew

# Add verbose logging to see what's failing
RUN ./gradlew bootJar --no-daemon -x test --info --stacktrace