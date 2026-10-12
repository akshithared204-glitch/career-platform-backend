# ----------------------------------------------------
# Stage 1: Build the application with Maven & JDK 21
# ----------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and pre-fetch dependencies for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package (skipping tests for production image)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ----------------------------------------------------
# Stage 2: Minimal runtime image with JRE 21
# ----------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as a non-privileged user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Render assigns a dynamic port via the PORT environment variable
ENV PORT=8082

# Activate the tracked deployment profile by default
ENV SPRING_PROFILES_ACTIVE=deploy

# JVM memory tuning for free-tier containers (e.g. 512 MB RAM limit on Render)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=40.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-deploy} -jar app.jar"]
