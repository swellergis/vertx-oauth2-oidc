FROM bitnami/java:17-debian-11

USER root
RUN apt-get update

ENV APP_DIR /application
ENV APP_FILE container-uber-jar.jar

WORKDIR $APP_DIR
RUN chmod -R go+r $APP_DIR
COPY target/*-fat.jar $APP_FILE
COPY src/main/dist/middleware.sh middleware.sh
RUN chmod +x middleware.sh

# changed perms so cert can be loaded into truststore
RUN chown 1001 /opt/bitnami/java/lib/security/cacerts

USER 1001
CMD ./middleware.sh
