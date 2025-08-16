# Используем многостадийную сборку для оптимизации размера образа
FROM eclipse-temurin:17-jdk-alpine as build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем Maven wrapper и файлы конфигурации
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn

# Делаем Maven wrapper исполняемым
RUN chmod +x ./mvnw

# Загружаем зависимости (для кеширования слоев)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src src

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Производственный образ
FROM eclipse-temurin:17-jre-alpine

# Устанавливаем curl для health check
RUN apk add --no-cache curl

# Создаем пользователя для запуска приложения (безопасность)
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR файл из стадии сборки
COPY --from=build /app/target/*.jar app.jar

# Изменяем владельца файлов
RUN chown -R appuser:appgroup /app

# Переключаемся на пользователя приложения
USER appuser

# Открываем порт 8080
EXPOSE 8080

# Настраиваем JVM для контейнера
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# Healthcheck для проверки состояния приложения
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Команда запуска приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
