FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .

# Download dependencies first to cache this layer
RUN mvn -B dependency:go-offline

# Now copy your source code and compile
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/noway-app-1.0.0.jar app.jar

# Explicitly match Render's environment variable mapping 
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
