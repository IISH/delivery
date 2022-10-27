#!/bin/bash

set -e

version=$(git rev-parse master)
tag=$(git describe --tags)
name="registry.diginfra.net/${USER}/delivery"

docker build --tag="${name}:${tag}" .
docker push "${name}:${tag}"

echo "RELEASE=${version} ${tag}"
