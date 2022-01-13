FROM openjdk:8 as base
RUN mkdir /app
COPY target/*jar-with-dependencies.jar /app/ldap.jar
ENTRYPOINT ["java","-jar", "/app/ldap.jar"]