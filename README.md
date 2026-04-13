# Delivery

## config

    -Dspring.config.additional-location=/path/to/folder/with/config

## Bouw de image

Maak een git tag voor de versie

    git tag -a vVersiepunten -m "vVersiepunten"

En bouw:

    version=$(git rev-parse master)
    tag=$(git describe --tags)
    name="registry.diginfra.net/${USER}/delivery"
    repo="${name}:${tag}"
    docker build --tag="$repo" .

Deploy image:

    docker push "$repo"

# NB Java 11 lokaal build

JAVA_HOME=pad-naar-java-11-sdk ./mvnw clean package -P jar -DskipTests

Voorbeeld:

    JAVA_HOME="${HOME}/.jdks/temurin-11.0.30" ./mvnw clean package -P jar -DskipTests


