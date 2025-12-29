# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN --mount=type=cache,target=/root/.gradle,id=gradle-cache,sharing=locked \
    ./gradlew dependencies --no-daemon || true

COPY src src

RUN --mount=type=cache,target=/root/.gradle,id=gradle-cache,sharing=locked \
    ./gradlew clean bootJar -x test --no-daemon  

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8087 

ENTRYPOINT ["java", "-jar", "app.jar"]