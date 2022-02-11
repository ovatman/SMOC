FROM alpine:edge

MAINTAINER celikelozdinc "ozdinc.celikel@gmail.com"

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

# Spring Boot #
ARG SPRINGBOOT_HOME=/opt/statemachineapp
COPY out/artifacts/LoadBalancer_jar/ $SPRINGBOOT_HOME/LoadBalancer_jar/
# Copy entrypoint script #
COPY loadbalancer/start_services.sh  $SPRINGBOOT_HOME/
RUN chmod +x $SPRINGBOOT_HOME/start_services.sh
WORKDIR $SPRINGBOOT_HOME
