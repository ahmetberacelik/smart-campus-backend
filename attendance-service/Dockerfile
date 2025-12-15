# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom and all module poms
COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY academic-service/pom.xml ./academic-service/pom.xml
COPY attendance-service/pom.xml ./attendance-service/pom.xml

# Create empty source directories for other modules to satisfy Maven
RUN mkdir -p api-gateway/src/main/java auth-service/src/main/java academic-service/src/main/java

# Copy attendance-service source code
COPY attendance-service/src ./attendance-service/src

# Build the application
RUN mvn clean package -pl attendance-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/attendance-service/target/*.jar app.jar

# Expose port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
