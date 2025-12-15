# Use Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install Maven (needed to run mvn inside container)
RUN apt-get update && apt-get install -y maven

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Expose Spring Boot port
EXPOSE 8080

# Run Spring Boot with DevTools
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.fork=false"]
