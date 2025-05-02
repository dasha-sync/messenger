# Talk Wire
.aka Telegram

## Environment:
- java 17
- spring 3.4.4
- maven
- swagger
- websockets
- postgresql
- rebbitmq

## Run all containers in the docker.
```sh
 $ docker compose up --build
```

## Run for local development, and dependencies in the docker.
```sh
 $ docker compose build
 $ docker compose up messenger-db-1
 $ mvn spring-boot:run
```

