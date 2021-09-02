#FROM adoptopenjdk/openjdk11:latest AS build
FROM openjdk:11 AS build

ARG GH_USER
ARG GH_TOKEN

RUN mkdir /src
COPY . /src
WORKDIR /src
RUN ./gradlew fulljar --no-daemon

#FROM alpine:3.14
FROM adoptopenjdk/openjdk11:alpine

RUN mkdir /app
COPY --from=build /src/build/libs/*.jar /app/application.jar

# copy crontabs for root user
COPY config/cronjobs /etc/crontabs/root

# start crond with log level 8 in foreground, output to stderr
CMD ["crond", "-f", "-d", "8"]