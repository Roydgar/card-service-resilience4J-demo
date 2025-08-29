# --- Stage 1: Build the JAR ---
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# --- Stage 2: Run the JAR ---
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java","-jar","app.jar"]