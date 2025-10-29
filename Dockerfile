# Estágio 1: Build (Construção)
# Use a imagem do Maven com o JDK 17. Altere '17' se usar outra versão do Java.
FROM maven:3.8.5-openjdk-17 AS builder

# Define o diretório de trabalho
WORKDIR /app

# Copia o pom.xml e baixa as dependências (para aproveitar o cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o restante do código-fonte e constrói o projeto
COPY src ./src
RUN mvn package -DskipTests

# Estágio 2: Run (Execução)
# Use uma imagem JRE leve (slim) para rodar a aplicação
FROM amazoncorretto:17-alpine

WORKDIR /app

# --- IMPORTANTE ---
# Altere 'target/meu-app-0.0.1-SNAPSHOT.jar' para o nome e caminho exato
# do arquivo .jar que o seu build do Maven gera!
COPY --from=builder /app/target/hat-api-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta 8080 (que o Spring usa por padrão)
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

