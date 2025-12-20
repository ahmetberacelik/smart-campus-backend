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
RUN mkdir -p api-gateway/src/main/java academic-service/src/main/java attendance-service/src/main/java meal-service/src/main/java event-service/src/main/java

# Copy auth-service source code
COPY auth-service/src ./auth-service/src

# Build the application
RUN mvn clean package -pl auth-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/auth-service/target/*.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]