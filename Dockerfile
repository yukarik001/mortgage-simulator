FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . /app
RUN chmod +x /app/gradlew
RUN /app/gradlew build --no-daemon
EXPOSE 8080
CMD ["/app/gradlew", "bootRun", "--no-daemon"]