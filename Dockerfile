# Build stage
FROM gradle:8.5.0-jdk21 AS builder
WORKDIR /app

# 全体コピー
COPY . .

# gradlewに権限を再付与（COPY後に失われるため）
RUN chmod +x gradlew

# ビルド実行
RUN ./gradlew build --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]