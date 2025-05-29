# VKontakte Clone - Запуск через Docker 🐳

## Быстрый старт

### Предварительные требования
- [Docker](https://docs.docker.com/get-docker/) 20.10+
- [Docker Compose](https://docs.docker.com/compose/install/) 2.0+

### Запуск одной командой

```bash
# Запуск приложения
./start.sh

# Или вручную
docker-compose up --build -d
```

### Остановка

```bash
# Остановка приложения
./stop.sh

# Или вручную
docker-compose down

# Полная очистка (включая данные БД)
docker-compose down --volumes
```

## Что включено

### 🗄️ База данных
- **PostgreSQL 15** в отдельном контейнере
- Автоматическое создание БД `vkontakte`
- Persistent storage для данных
- Health check для проверки готовности

### 🚀 Приложение
- **Spring Boot** приложение в контейнере
- Автоматическая сборка из исходного кода
- Подключение к PostgreSQL
- Инициализация тестовых данных

## Доступ к приложению

После запуска приложение будет доступно по адресу:
**http://localhost:8081**

### 🔑 Тестовые аккаунты

| Роль | Логин | Пароль |
|------|-------|--------|
| Администратор | `admin` | `admin123` |
| Ученик | `student1` | `student123` |
| Ученик | `student2` | `student123` |

## Полезные команды

### Просмотр логов
```bash
# Все логи
docker-compose logs -f

# Логи только приложения
docker-compose logs -f app

# Логи только БД
docker-compose logs -f postgres
```

### Статус контейнеров
```bash
docker-compose ps
```

### Подключение к базе данных
```bash
# Через psql в контейнере
docker-compose exec postgres psql -U vkontakte_user -d vkontakte

# Или через внешний клиент
# Host: localhost
# Port: 5432
# Database: vkontakte
# Username: vkontakte_user
# Password: vkontakte_password
```

### Перезапуск только приложения
```bash
docker-compose restart app
```

### Пересборка образа
```bash
docker-compose build --no-cache app
docker-compose up -d app
```

## Структура Docker

### Dockerfile
- Базовый образ: `openjdk:17-jdk-slim`
- Многоэтапная сборка для оптимизации
- Кэширование Maven зависимостей
- Автоматическая сборка JAR файла

### docker-compose.yml
- **postgres**: База данных PostgreSQL
- **app**: Spring Boot приложение
- **vkontakte_network**: Изолированная сеть
- **postgres_data**: Persistent volume для данных

## Переменные окружения

Приложение настраивается через переменные окружения в `docker-compose.yml`:

```yaml
environment:
  # База данных
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/vkontakte
  SPRING_DATASOURCE_USERNAME: vkontakte_user
  SPRING_DATASOURCE_PASSWORD: vkontakte_password
  
  # JPA
  SPRING_JPA_DATABASE_PLATFORM: org.hibernate.dialect.PostgreSQLDialect
  SPRING_JPA_HIBERNATE_DDL_AUTO: create-drop
  
  # Сервер
  SERVER_PORT: 8081
```

## Troubleshooting

### Приложение не запускается
1. Проверьте логи: `docker-compose logs app`
2. Убедитесь, что PostgreSQL запустился: `docker-compose logs postgres`
3. Проверьте порты: `docker-compose ps`

### База данных недоступна
1. Проверьте health check: `docker-compose ps`
2. Перезапустите БД: `docker-compose restart postgres`
3. Проверьте логи БД: `docker-compose logs postgres`

### Порт уже занят
```bash
# Найти процесс на порту 8081
sudo lsof -i :8081

# Изменить порт в docker-compose.yml
ports:
  - "8082:8081"  # Внешний порт 8082
```

### Очистка всех данных
```bash
# Остановка и удаление всего
docker-compose down --volumes --rmi all

# Очистка неиспользуемых образов
docker system prune -a
```

## Производственное развертывание

Для продакшена рекомендуется:

1. **Изменить пароли БД**
2. **Использовать внешний PostgreSQL**
3. **Настроить SSL/TLS**
4. **Добавить мониторинг**
5. **Настроить backup БД**

### Пример для продакшена
```yaml
environment:
  SPRING_JPA_HIBERNATE_DDL_AUTO: validate  # Не пересоздавать БД
  LOGGING_LEVEL_COM_EXAMPLE_DEMO: WARN     # Меньше логов
  SPRING_JPA_SHOW_SQL: false               # Отключить SQL логи
```

---

**Готово к использованию!** 🎉 
Запустите `./start.sh` и откройте http://localhost:8081 