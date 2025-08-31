FROM openjdk:17-jdk-slim
WORKDIR /app

#  Gradle wrapper and files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# Run the JAR
ENTRYPOINT ["java","-jar","build/libs/stackquiz-api-0.0.1-SNAPSHOT.jar"]
