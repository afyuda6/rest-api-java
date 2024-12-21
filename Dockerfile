FROM openjdk:17-jdk-slim

ENV LANG=C.UTF-8

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y sqlite3 curl && \
    apt-get clean

WORKDIR /rest-api-java

COPY out/production/rest-api-java/ /rest-api-java/

RUN curl -L -o /rest-api-java/sqlite-jdbc.jar https://github.com/xerial/sqlite-jdbc/releases/download/3.36.0.3/sqlite-jdbc-3.36.0.3.jar

EXPOSE 8080

CMD ["java", "-cp", ".:/rest-api-java/sqlite-jdbc.jar", "Main"]