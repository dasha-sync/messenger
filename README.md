# Talk Wire
a.k.a. Telegram

[Infrastructure](https://github.com/dasha-sync/messenger-infra)
[Frontend](https://github.com/dasha-sync/messenger-front)

## Environment:
- java 17
- spring 3.4.4
- swagger
- websockets
- postgresql
- rebbitmq

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
