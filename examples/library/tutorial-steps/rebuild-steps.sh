#!/usr/bin/env bash

dest="."
if [[ "$#" -eq 1 ]]; then
  dest=$1
fi

here=$(pwd)

empty="$dest/__blank"
rm -rf ${empty}
mkdir -p ${empty}

src=${empty}

FILES=_patches/*
for path in ${FILES}
do
  filename=$(basename ${path})
  base=${filename%.*}
  newsrc="$dest/$base"
#  cp -R ${src} ${newsrc}

  rsync -a ${src}/ ${newsrc}/

  echo "Applying $base to $src in $newsrc"
  src=${newsrc}
  patch -p1 -d ${src} -i "${here}/${path}"
done

echo "Done"

rm -rf ${empty}
