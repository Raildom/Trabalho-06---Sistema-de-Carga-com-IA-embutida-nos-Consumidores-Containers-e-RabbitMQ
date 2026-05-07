# Stage 1: build com Maven
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copia apenas o pom.xml
COPY pom.xml .

# 2. Baixa dependências (cacheável)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# 3. Copia o código fonte só depois
COPY src ./src

# 4. Compila e gera o JAR com dependências
RUN --mount=type=cache,target=/root/.m2 mvn clean package assembly:single -B


# Stage 2: runtime leve
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Copia o JAR compilado do stage anterior
COPY --from=build /app/target/consumerSign-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# Copia recursos (dataset, modelo)
COPY src/main/resources/ src/main/resources/

CMD ["java", "-jar", "app.jar"]
