#!/bin/sh

rm -rf .jekyll-metadata _site
jekyll serve --livereload --incremental

