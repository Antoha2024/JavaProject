FROM node:18-alpine

WORKDIR /app

# Копируем и устанавливаем зависимости
COPY package*.json ./
RUN npm ci --only=production

# Копируем исходный код
COPY server.js .

EXPOSE 8080

CMD ["node", "server.js"]