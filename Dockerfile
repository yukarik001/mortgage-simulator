# Build stage
FROM gradle:8.0.2-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]