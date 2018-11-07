#!/usr/bin/env bash

steps=(1-initial-setup 2-main-class 3-user-context 4-creating-a-resource 5-response-body)

prev=""
for step in "${steps[@]}"
do
  ./create-patch.sh ${prev} ${step}
  prev=${step}
done

