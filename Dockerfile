# Build stage
FROM gradle:8.5.0-jdk21 AS builder
WORKDIR /app

# gradlewを先にコピーして権限付与
COPY gradlew /app/gradlew
RUN chmod +x /app/gradlew

# 残りのファイルをコピー
COPY . .

# ビルド実行
RUN ./gradlew build --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
