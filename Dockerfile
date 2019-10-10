FROM openjdk:8-jre-slim

RUN apt-get update \
  && apt-get -y install gettext-base \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/data \
  mkdir -p /opt/models/backup \
  mkdir -p /opt/data/data/OpenNLP/ner \
  mkdir -p /opt/data/data/OpenNLP/dict 

COPY docker/docker-application.properties.tmpl /conf/
COPY docker/init-models.sh /usr/local/bin
ADD docker/docker-entrypoint.sh /usr/local/bin
ADD target/slotex-nlp-core*.jar /opt/app.jar
ADD models/* /opt/data/models/
ADD models/backup/* /opt/data/models/backup/
ADD data/OpenNLP/dict/* /opt/data/data/OpenNLP/dict/
ADD data/OpenNLP/ner/* /opt/data/data/OpenNLP/ner/

VOLUME /tmp

# http
EXPOSE 8100

ENTRYPOINT ["docker-entrypoint.sh"]
#CMD ["java","-Djava.security.egd=file:/dev/./urandom", "-Djava.net.preferIPv4Stack=true", "-Djboss.server.config.dir=/conf", "-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap","-XX:+ExitOnOutOfMemoryError",i "-XX:+CrashOnOutOfMemoryError", "-XX:MinHeapFreeRatio=20", "-XX:MaxHeapFreeRatio=40", "-XX:GCTimeRatio=4", "-XX:AdaptiveSizePolicyWeight=90", "-jar", "/opt/app.jar", "--spring.config.location=file:/conf/application.properties"]
