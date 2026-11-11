FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/notification-service-1.0.0.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar","/app/app.jar"]
