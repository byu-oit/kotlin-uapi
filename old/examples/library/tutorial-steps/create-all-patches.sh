#!/usr/bin/env bash

mvn clean

steps=(1-initial-setup 2-main-class 3-user-context 4-creating-a-resource 5-response-body 6-listing-resources 7-mutating-resources)

prev=""
for step in "${steps[@]}"
do
  create-patch.sh ${prev} ${step}
  prev=${step}
done

