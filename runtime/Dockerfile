FROM adoptopenjdk/openjdk8:jre8u382-b05-alpine
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories
RUN apk update && apk add --no-cache supervisor
WORKDIR /agent-runtime
ENV TZ Asia/Shanghai
ADD ./target/runtime-1.0.0-SNAPSHOT-fat.jar ./app.jar
ADD ./supervisord.conf /etc/supervisord.conf
RUN nohup /usr/bin/supervisord -c /etc/supervisord.conf &
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]