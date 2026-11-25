# Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests

# Run (Hugging Face Docker Spaces: port 7860, non-root user with UID 1000)
FROM eclipse-temurin:21-jre
RUN useradd -m -u 1000 app || true
WORKDIR /app
COPY --from=build /src/target/java-code-reviewer-*.jar app.jar
USER 1000
EXPOSE 7860
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
