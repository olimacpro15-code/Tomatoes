# ---------- Etapa 1: compilar el JAR con Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---------- Etapa 2: imagen liviana de ejecución ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/ms-empleados-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT [ "java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar" ]
