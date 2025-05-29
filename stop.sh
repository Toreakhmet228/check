#!/bin/bash

echo "🛑 Остановка VKontakte Clone..."
echo "================================"

# Останавливаем контейнеры
docker-compose down

echo "✅ VKontakte Clone остановлен!"
echo ""
echo "📝 Для полной очистки (включая данные БД) используйте:"
echo "   docker-compose down --volumes" 