#!/bin/bash 
###################################################################
#Script Name   : docker-entrypoint.sh
#Description   : Docker entrypoint
#Args          :
#Author        : Tadej Justin                                                
#Email         : tadej.justin@medius.si
###################################################################


./init-models.sh
envsubst < /conf/docker-application.properties.tmpl > /conf/docker-application.properties
java -Djava.security.egd=file:/dev/./urandom \
     -Djava.net.preferIPv4Stack=true \
     -Djboss.server.config.dir=/conf \
     -XX:+ExitOnOutOfMemoryError \
     -XX:+CrashOnOutOfMemoryError \
     -XX:GCTimeRatio=4 \
     -XX:AdaptiveSizePolicyWeight=90 \
     -cp app/libs/*:app/resources:app/classes si.slotex.nlp.Application \
     --spring.config.location=file:/conf/docker-application.properties

exit 0
