#!/bin/bash

./gradlew clean bootJar

tag=$(git describe --tags)
name="registry.diginfra.net/edepot/delivery"

docker build --tag="${name}:${tag}" .
docker push "${name}:${tag}"
