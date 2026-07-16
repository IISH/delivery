#!/bin/bash

./gradlew clean bootJar

tag=$(git describe --tags)
name="registry.diginfra.net/edepot/delivery"

docker build --tag="${name}:${tag}" --no-cache .
docker push "${name}:${tag}"
