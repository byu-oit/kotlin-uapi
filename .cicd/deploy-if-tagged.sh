#!/usr/bin/env bash

if [[ ${CODEBUILD_WEBHOOK_TRIGGER} == tag/* ]]; then
    echo "Deploying tagged release"
    ./mvnw clean install
else
    echo "Skipping deployment for ref $CODEBUILD_WEBHOOK_TRIGGER"
fi
