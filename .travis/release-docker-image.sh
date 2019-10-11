#!/bin/bash 
###################################################################
#Script Name   : release-docker-image.sh
#Description   : Release docker image to docker registry
#Args          :
#Author        : Tadej Justin                                                
#Email         : tadej.justin@medius.si
###################################################################

set -e

if [ -z "${TRAVIS_TAG}" ]; then 
    echo "We do not have travis tag, thish should not happen."
    exit 1
fi
VERSION="${TRAVIS_TAG//v}"
echo "Ensuring that pom  matches $TRAVIS_TAG"
mvn org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=$VERSION
docker-compose up -d mongodb redis
mvn clean package 
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker build -t $IMAGE_NAME:$TRAVIS_TAG
docker push $IMAGE_NAME:$TRAVIS_TAG
docker-compose down

