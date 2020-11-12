FROM openjdk:11-jdk-slim AS build

COPY . /app
WORKDIR /app

RUN ./mvnw install -P jar -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:11-jdk-slim

RUN apt-get update -y && apt-get install -y fontconfig libfreetype6 cups cups-bsd cups-client

COPY --from=build /app/target/dependency/BOOT-INF/classes /app
COPY --from=build /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /app/target/dependency/META-INF /app/META-INF

COPY run.sh /opt
RUN chmod +x /opt/run.sh

ENTRYPOINT ["/opt/run.sh"]
