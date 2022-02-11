FROM alpine:edge

MAINTAINER akyuzj "akyuzj@itu.edu.tr"

ARG JDK_VERSION=openjdk8

RUN \
  apk update && \
  apk add --no-cache bash && \
  apk add --no-cache busybox-extras && \
  apk add --no-cache openrc

 # install java #
RUN \
  apk add --no-cache ${JDK_VERSION}

  # clean up #
RUN \
  rm -rf /var/cache/apk*

EXPOSE 27017

# Spring Boot #
ARG SPRINGBOOT_HOME=/opt/statemachineapp
COPY build/libs/ $SPRINGBOOT_HOME/TwitterStateMachine_jar/
# Copy entrypoint script #
COPY twittersm/start_services.sh $SPRINGBOOT_HOME/
RUN chmod +x $SPRINGBOOT_HOME/start_services.sh

COPY higgs-activity_time.txt $SPRINGBOOT_HOME/
COPY higgs-social_network.edgelist $SPRINGBOOT_HOME/

WORKDIR $SPRINGBOOT_HOME

ENTRYPOINT ["/bin/bash", "./start_services.sh"]
