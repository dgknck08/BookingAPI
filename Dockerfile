# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build

# Maven kurulumu
RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean install -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
