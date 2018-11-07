#!/usr/bin/env bash

blank_dir=""

if [[ "$#" -eq 1 ]]; then
  blank_dir="__blank"
  rm -rf ${blank_dir}
  mkdir -p ${blank_dir}

  src=${blank_dir}
  dest=$1
  echo "Diffing (blank dir) -> ${dest}"
elif [[ "$#" -eq 2 ]]; then
  src=$1
  dest=$2
  echo "Diffing $src -> ${dest}"
else
  echo "Usage is create-patch.sh [old-version] new-version"
  exit 1
fi

diff_file="_patches/${dest}.patch"

diff -urN -x '*.iml' -x 'target/*' ${src} ${dest} > ${diff_file}

echo "Wrote diff to ${diff_file}"

if [[ !  -z  ${blank_dir} ]]; then
  echo "Cleaning up blank dir"
  rm -rf ${blank_dir}
fi
