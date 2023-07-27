FROM mini-agent-runtime:latest
WORKDIR /app
ENV TZ Asia/Shanghai
ADD ./target/demo-1.0-SNAPSHOT-fat.jar ./app.jar
ENTRYPOINT ["mini-agent-runtime-starter", "java", "-jar", "app.jar"]