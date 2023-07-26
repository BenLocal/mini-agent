FROM mini-agent-runtime:latest
WORKDIR /app
ENV TZ Asia/Shanghai
ADD ./target/demo-1.0-SNAPSHOT-fat.jar ./app.jar
ENTRYPOINT ["nohup", "/usr/bin/supervisord","-c", "/etc/supervisord.conf", "java", "-jar", "app.jar"]