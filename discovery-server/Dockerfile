FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Создаём структуру папок
RUN mkdir -p src/main/java/com/discovery

# Копируем исходный код сервера
COPY src ./src

# Компилируем сервер
RUN javac -d ./classes $(find ./src -name "*.java")

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Копируем скомпилированные классы
COPY --from=build /app/classes ./classes

EXPOSE 8765

# Запускаем сервер
ENTRYPOINT ["java", "-cp", "./classes", "com.discovery.DiscoveryServer"]