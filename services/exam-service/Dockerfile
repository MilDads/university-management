FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/exam-service.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java","-jar","app.jar"]
