# ---------- Etapa 1: build ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copia só o necessário para baixar as dependências (aproveita o cache do Docker)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Agora o código-fonte e o empacotamento
COPY src/ src/
RUN ./mvnw -B -q clean package -DskipTests

# ---------- Etapa 2: execução ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

ENV TZ=UTC

# Usuário sem privilégios (não roda como root)
RUN useradd --system --create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]