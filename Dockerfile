FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY . .
RUN ./mvnw clean package -DskipTests

##########################################
# STAGE 3 — FINAL IMAGE
##########################################
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Копируем собранный jar
COPY --from=backend-build /app/target/*.jar app.jar

# Настройки окружения
ENV SPRING_DATASOURCE_URL=jdbc:p6spy:postgresql://ep-rough-king-adezmgz4-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require
ENV SPRING_DATASOURCE_USERNAME=neondb_owner
ENV SPRING_DATASOURCE_PASSWORD=npg_fntKcNsg32zF
ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.p6spy.engine.spy.P6SpyDriver
ENV SERVER_PORT=8080
EXPOSE 8080

# ВАЖНО: добавляем путь static в classpath при запуске
ENTRYPOINT ["java", "-cp", "app.jar:/app", "org.springframework.boot.loader.launch.JarLauncher"]
