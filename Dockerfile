FROM eclipse-temurin:23.0.2_7-jdk as delivery

RUN apt-get update -y && \
    apt-get upgrade -y && \
    fc-cache -f -v && \
    mkdir -p /app/config && \
    chown 1000:1000 /app && \
    touch /home/ubuntu/.mime.types

VOLUME /app/config

WORKDIR /app

USER ubuntu

ENV SPRING_PROFILES_ACTIVE=development
ENV JAVA_OPTS='-Dhello=world'

ENTRYPOINT ["/entrypoint.sh"]

CMD [""]

COPY entrypoint.sh /entrypoint.sh

# via  ./gradlew clean bootJar
COPY build/libs/delivery.jar /app/delivery.jar