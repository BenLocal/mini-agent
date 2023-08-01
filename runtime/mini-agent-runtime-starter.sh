#!/bin/bash

echo "****start runtime ****"
echo Script name: $0
echo Script $# arguments
echo Script arguments: $@

RUNTIME_AGES="-Dmini.agent.config.path=/etc/mini-agent"
######################
# env
######################

# appid
[ -z "$ARUNTIME_APP_ID" ] || RUNTIME_AGES="$RUNTIME_AGES --appId $ARUNTIME_APP_ID"
echo "ARUNTIME_APP_ID is $ARUNTIME_APP_ID"

# agent-http-port
[ -z "$ARUNTIME_AGENT_HTTP_PORT" ] || RUNTIME_AGES="$RUNTIME_AGES --agent-http-port $ARUNTIME_AGENT_HTTP_PORT"
echo "ARUNTIME_AGENT_HTTP_PORT is $ARUNTIME_AGENT_HTTP_PORT"

# http-port
[ -z "$ARUNTIME_HTTP_PORT" ] || RUNTIME_AGES="$RUNTIME_AGES --http-port $ARUNTIME_HTTP_PORT"
echo "ARUNTIME_HTTP_PORT is $ARUNTIME_HTTP_PORT"

echo "args: $RUNTIME_AGES"
java -jar /agent-runtime/app.jar run org.mini.agent.runtime.MainVerticle \
    $RUNTIME_AGES \
    2>&1 &

echo "**** start run cmd: '$@' ****"
exec "$@" 2>&1 &
wait $!
exit $?

