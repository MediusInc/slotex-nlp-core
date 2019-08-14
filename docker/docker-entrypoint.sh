#!/bin/bash 
###################################################################
#Script Name   : docker-entrypoint.sh
#Description   : Docker entrypoint
#Args          :
#Author        : Tadej Justin                                                
#Email         : tadej.justin@medius.si
###################################################################


/usr/local/bin/init-models.sh
envsubst < /conf/docker-application.properties.tmpl > /conf/docker-application.properties
java -Djava.security.egd=file:/dev/./urandom \
     -Djava.net.preferIPv4Stack=true \
     -Djboss.server.config.dir=/conf \
     -XX:+UnlockExperimentalVMOptions \
     -XX:+UseCGroupMemoryLimitForHeap \
     -XX:+ExitOnOutOfMemoryError \
     -XX:+CrashOnOutOfMemoryError \
     -XX:MinHeapFreeRatio=20 \
     -XX:MaxHeapFreeRatio=40 \
     -XX:GCTimeRatio=4 \
     -XX:AdaptiveSizePolicyWeight=90 \
     -jar /opt/app.jar \
     --spring.config.location=file:/conf/docker-application.properties

exit 0
