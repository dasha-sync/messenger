# Talk Wire
.aka Telegram

## Environment:
- java 17
- spring 3.4.4
- maven 4
- swagger
- websockets
- postgresql
- rebbitmq

## Run all containers in the docker.
```sh
 $ mvn clean install
 $ docker compose up --build
 $ ./mvnw spring-boot:run
```

## Run for local development, and dependencies in the docker.
```sh
 $ mvn clean install
 $ docker compose up --build
```
Find messenger-app container and stop it.
```sh
 $ ./mvnw spring-boot:run
```

