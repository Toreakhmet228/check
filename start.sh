#!/bin/bash

echo "🚀 Запуск VKontakte Clone..."
echo "================================"

# Проверяем, установлен ли Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен. Пожалуйста, установите Docker."
    exit 1
fi

# Проверяем, установлен ли Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose не установлен. Пожалуйста, установите Docker Compose."
    exit 1
fi

# Останавливаем и удаляем существующие контейнеры
echo "🧹 Очистка существующих контейнеров..."
docker-compose down --volumes

# Собираем и запускаем контейнеры
echo "🔨 Сборка и запуск контейнеров..."
docker-compose up --build -d

# Ждем запуска базы данных
echo "⏳ Ожидание запуска базы данных..."
sleep 10

# Проверяем статус контейнеров
echo "📊 Статус контейнеров:"
docker-compose ps

echo ""
echo "✅ VKontakte Clone запущен!"
echo "🌐 Откройте браузер и перейдите по адресу: http://localhost:8081"
echo ""
echo "🔑 Тестовые аккаунты:"
echo "   Администратор: admin / admin123"
echo "   Ученик 1: student1 / student123"
echo "   Ученик 2: student2 / student123"
echo ""
echo "📝 Для остановки используйте: docker-compose down"
echo "📊 Для просмотра логов: docker-compose logs -f" 