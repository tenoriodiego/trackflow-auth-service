
# =========================================
# Build stage
# =========================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src

# Os testes já foram executados localmente.
# Não executar Testcontainers durante o build remoto.
RUN ./mvnw clean package -DskipTests -B

# =========================================
# Runtime stage
# =========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
