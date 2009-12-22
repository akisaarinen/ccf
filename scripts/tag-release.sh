#!/bin/bash
if [ $# -lt 1 ] || [ $# -gt 2 ] 
then
  echo "Usage: ${0} <tagged-version> [<next-devel-version>]"
  exit
fi

VERSION=$1
BUILD_FILE="project/build.properties"

if [ ! -f $BUILD_FILE ] 
then
  echo "Error: project file $BUILD_FILE not found"
  exit
fi

ORIGINAL_VERSION=`grep "project.version=" $BUILD_FILE`

if [ $# == 2 ] 
then
  NEXT_VERSION=$2
else
  NEXT_VERSION=$ORIGINAL_VERSION
fi

echo "Updating project file $BUILD_FILE"

sed -i "s/^project.version=.*$/project.version=${1}/" $BUILD_FILE
git add project/build.properties
git commit -s -m "Update project version to ${1} for release"
git push origin master

git tag "v${VERSION}"
git push --tags origin master

sed -i "s/^project.version=.*$/project.version=${NEXT_VERSION}/" $BUILD_FILE
git add project/build.properties
git commit -s -m "Update project version back to development snapshot"
git push origin master


