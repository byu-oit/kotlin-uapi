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

  working="$newsrc-working"

  cp -R ${src} ${working}

  echo "Applying $base to $src in $working"
  src=${newsrc}
  patch -p1 -N -d ${working} -i "${here}/${path}"

  rsync -a --exclude '*.iml' ${working}/ ${newsrc}/
  rm -rf ${working}
done

echo "Done"

rm -rf ${empty}
