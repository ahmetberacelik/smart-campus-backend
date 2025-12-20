# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom and all module poms
COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY academic-service/pom.xml ./academic-service/pom.xml
COPY attendance-service/pom.xml ./attendance-service/pom.xml
COPY meal-service/pom.xml ./meal-service/pom.xml
COPY event-service/pom.xml ./event-service/pom.xml

# Create empty source directories for other modules to satisfy Maven
RUN mkdir -p auth-service/src/main/java academic-service/src/main/java attendance-service/src/main/java meal-service/src/main/java event-service/src/main/java

# Copy api-gateway source code
COPY api-gateway/src ./api-gateway/src

# Build the application
RUN mvn clean package -pl api-gateway -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/api-gateway/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]