FROM eclipse-temurin:17-jre
WORKDIR /app

# Use wildcard to match any JAR file
COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]