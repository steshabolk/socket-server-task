## Bank Socket Server

### Использование

Запуск сервиса с помощью [docker-compose](./docker-compose.yml):

```
docker compose up -d
```

- 3 контейнера: api, база данных postgresql, [liquibase](./migrations/scripts) миграции для инициализации базы данных
- сервис доступен по ссылке : http://localhost:8087
- [postman collection](./server.postman_collection.json)

### Описание

- [функции](./src/main/java/com/task/server/handler/endpoint) пользователя : регистрация, аутентификация, проверка
  баланса, перевод средств другому пользователю
- сервер реализован с помощью [ServerSocket](./src/main/java/com/task/server/service/SocketServer.java)
- запросы в базу данных с помощью [JDBC](./src/main/java/com/task/server/dao)
- использование [JWT-токена](./src/main/java/com/task/server/util/JwtUtil.java)
- [логирование](./src/main/resources/log4j2.xml) в файл и консоль
- обработка [исключений](./src/main/java/com/task/server/exception/ApiExceptionType.java): невалидные данные
  http-запроса, валидация токена, повторная регистрация, некорректные данные
  пользователя (логин / пароль), недостаточно средств при переводе
