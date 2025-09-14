FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . /app
RUN ./gradlew build --no-daemon
CMD ["./gradlew", "bootRun", "--no-daemon"]