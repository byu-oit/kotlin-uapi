#!/usr/bin/env bash

ref=${CODEBUILD_WEBHOOK_TRIGGER}
ver=`./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout`

isTag () {
    [[ "$1" == tag/* ]]
}

isDev () {
    [[ "$1" == branch/develop ]]
}

isSnapshot () {
    [[ "$1" == *-SNAPSHOT ]]
}

if isTag $ref; then
    echo "Deploying tagged release"
    ./mvnw clean deploy
elif isDev "$ref" && isSnapshot "$ver"; then
    echo "Deploying snapshot from branch/develop"
    ./mvnw clean deploy
else
    echo "Skipping deployment for ref $ref and project version $ver"
fi
