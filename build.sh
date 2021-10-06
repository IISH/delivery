#!/bin/bash

version=$(git rev-parse master)
tag=$(git describe --tags)
name="registry.diginfra.net/${USER}/delivery"

docker_tag=${tag:1}
docker build --tag="${name}:${docker_tag}" .
docker push "${name}:${docker_tag}"

echo "RELEASE=${version} ${tag}"
