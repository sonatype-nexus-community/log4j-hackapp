FROM openjdk:8 as base
COPY target/*jar-with-dependencies.jar /app/app.jar
ENTRYPOINT ["java","-jar", "/app/app.jar"]