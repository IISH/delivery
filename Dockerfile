FROM eclipse-temurin:23.0.2_7-jdkn

RUN apt-get update -y && \
    apt-get upgrade -y && \
    apt-get install -y fontconfig libfreetype6 fonts-liberation cups cups-bsd cups-client && \
    printf '%s\n' \
        '<?xml version="1.0"?>'\
        '<!DOCTYPE fontconfig SYSTEM "fonts.dtd">' \
        '<fontconfig>' \
        ' <alias>' \
        ' <family>sans-serif</family>' \
        ' <prefer>' \
        ' <family>Liberation Sans</family>' \
        ' </prefer>' \
        ' </alias>' \
        '</fontconfig>' > /etc/fonts/local.conf && \
    fc-cache -f -v && \
    mkdir -p /app/config && \
    chown 1000:1000 /app

VOLUME /app/config

WORKDIR /app

USER ubuntu

ENV SPRING_PROFILES_ACTIVE=development
ENV JAVA_OPTS='-Dhello=world'

ENTRYPOINT ["/entrypoint.sh"]

CMD [""]

COPY entrypoint.sh /entrypoint.sh

# via  ./gradlew clean bootJar
COPY build/libs/delivery.jar  /app/delivery.jar
