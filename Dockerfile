FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the pre-built JAR (built by GitHub Actions)
COPY build/libs/*.jar app.jar

# Run the application (your external Redis will be available)
ENTRYPOINT ["java", "-jar", "app.jar"]