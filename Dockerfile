# Используем официальный образ OpenJDK 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем Maven wrapper и pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Делаем mvnw исполняемым
RUN chmod +x ./mvnw

# Загружаем зависимости (для кэширования слоев)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Экспонируем порт
EXPOSE 8081

# Запускаем приложение
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"] 