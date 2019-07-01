#!/bin/sh

if [ $# -lt 2 ]; then
  echo "usage is copy-step.sh from-step to-step"
  exit 1
fi

cp -R $1 $2
rm -rf $2/target $2/*.iml
sed -i '' -e "s/$1/$2/g" $2/pom.xml
sed -i '' -e "s/<module>$1<\/module>/<module>$1<\/module><module>$2<\/module>/" ./pom.xml

git add $2

