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

