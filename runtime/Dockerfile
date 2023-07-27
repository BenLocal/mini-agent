FROM adoptopenjdk/openjdk8:jre8u382-b05-alpine
WORKDIR /agent-runtime
ENV TZ Asia/Shanghai
ADD ./target/runtime-1.0.0-SNAPSHOT-fat.jar ./app.jar
ADD ./mini-agent-runtime-starter.sh ./mini-agent-runtime-starter
RUN chmod +x ./mini-agent-runtime-starter && \
    cp ./mini-agent-runtime-starter /usr/bin/mini-agent-runtime-starter
# ln -f -s /agent-runtime/mini-agent-runtime-starter /usr/bin/mini-agent-runtime-starter

RUN ls /usr/bin/mini-agent-runtime-starter
ENTRYPOINT [ "java", "-jar", "./app.jar" ]