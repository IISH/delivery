FROM eclipse-temurin:25.0.4.1_1-jdk-noble AS build
RUN apt-get update -y && \
    apt-get upgrade -y

#RUN javac --version

COPY ./ /delivery

WORKDIR /delivery

RUN ./gradlew clean bootJar

FROM eclipse-temurin:25.0.4.1_1-jdk-noble AS delivery

RUN apt-get update -y && \
    apt-get upgrade -y && \
    apt-get install -y fontconfig libfreetype6 fonts-liberation cups-bsd cups-client && \
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

COPY --from=build /delivery/entrypoint.sh /entrypoint.sh

# via  ./gradlew clean bootJar
COPY --from=build /delivery/build/libs/delivery.jar /app/delivery.jar