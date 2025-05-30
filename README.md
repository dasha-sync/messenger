# Talk Wire
a.k.a. Telegram

[Infrastructure](https://github.com/dasha-sync/messenger-infra)
[Frontend](https://github.com/dasha-sync/messenger-front)

## Environment:
- Java 17
- Spring 3.4.4
- Swagger
- Websockets
- PostgreSQL
- JWT, HttpOnly cookies

## Run in docker.
```sh
 $ docker compose up --build
```

## Or local development.
```sh
 $ docker compose build
 $ docker compose up messenger-db-1
 $ mvn spring-boot:run
```
