# Stage 1: Build React/Vite app
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
RUN npm install -g vite
COPY . .
RUN npm run build

# Stage 2: Nginx
FROM nginx:1.25-alpine

# Копируем свой конфиг поверх стандартного
COPY nginx.conf /etc/nginx/nginx.conf

# Копируем билд
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
