FROM adoptopenjdk/openjdk8:jdk8u382-b05-centos-slim
WORKDIR /agent-runtime

# https://stackoverflow.com/questions/66357779/lc-all-cannot-change-locale-en-us-utf-8-newaliases-in-ssmtp-aliases-are-read
ENV LC_ALL=C
ENV LANG=en_US.utf-8
ENV TZ Asia/Shanghai

ADD ./target/runtime-1.0.0-SNAPSHOT-fat.jar ./app.jar
ADD ./mini-agent-runtime-starter.sh ./mini-agent-runtime-starter
RUN chmod +x ./mini-agent-runtime-starter && \
    cp ./mini-agent-runtime-starter /usr/bin/mini-agent-runtime-starter

RUN /bin/bash -c set -eux;
CMD ["/bin/bash" ]