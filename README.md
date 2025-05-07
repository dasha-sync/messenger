# Talk Wire
a.k.a. Telegram

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

