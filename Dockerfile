# Build and run PantryPal in a container (used by Render and other hosts).
# Stage 1: build the jar with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Stage 2: run it on a small Java runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/pantrypal-1.0.0.jar app.jar
# Free hosting plans have ~512 MB of memory; keep Java within it
ENV JAVA_OPTS="-Xmx300m -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
# The host sets PORT; application.properties reads it (server.port=${PORT:8080})
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
