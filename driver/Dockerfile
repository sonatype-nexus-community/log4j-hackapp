FROM openjdk:8 as base
RUN apt-get update && apt-get install -y curl
RUN curl -fsSL https://get.docker.com -o get-docker.sh
RUN sh ./get-docker.sh
FROM openjdk:8 as runner
COPY --from=base /usr/bin/docker  /usr/bin/docker
RUN mkdir -p /driver/log4jversions
RUN mkdir -p /driver/runner
COPY log4jversions/*/target/*-jar-with-dependencies.jar /driver/log4jversions
COPY runner/target/*.jar /driver/runner/runner.jar
COPY server/target/*jar-with-dependencies.jar /app/server.jar

ENTRYPOINT ["java","-jar", "/app/server.jar"]