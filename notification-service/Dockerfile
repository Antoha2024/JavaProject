# Используем образ с Maven и JDK 17 для сборки
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем файлы pom.xml и исходный код
COPY pom.xml .
COPY src ./src

# Собираем WAR-файл, пропуская тесты (для скорости сборки)
RUN mvn clean package -DskipTests

# Используем минимальный образ JRE для запуска
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Копируем собранный WAR-файл из этапа сборки
COPY --from=build /app/target/*.war app.war

# Tomcat по умолчанию использует 8080 порт
EXPOSE 8080

# Запускаем приложение через встроенный Tomcat
ENTRYPOINT ["java", "-jar", "app.war"]