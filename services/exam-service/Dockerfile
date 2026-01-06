FROM gradle:jdk25 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle clean build -x test --no-daemon

FROM openjdk:25-jdk-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]