FROM adoptopenjdk/openjdk8:jre8u382-b05-alpine

WORKDIR /app

ENV TZ Asia/Shanghai

ADD ./target/demo-1.0-SNAPSHOT-fat.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]