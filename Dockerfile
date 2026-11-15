# syntax=docker/dockerfile:1
# HAT API (indicadores, só leitura do SPData). Nenhuma credencial na imagem: variáveis chegam no docker compose.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/hat-api-0.0.1-SNAPSHOT.jar app.jar
USER app
EXPOSE 8085
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
