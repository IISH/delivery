FROM openjdk:11-jdk-slim AS build

COPY . /app
WORKDIR /app

RUN ./mvnw install -P jar -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:11-jdk-slim

COPY --from=build /app/target/dependency/BOOT-INF/classes /app
COPY --from=build /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /app/target/dependency/META-INF /app/META-INF

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "/app:/app/lib/*", "org.socialhistoryservices.delivery.Application"]
