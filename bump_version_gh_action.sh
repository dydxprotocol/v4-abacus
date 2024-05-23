#!/bin/sh

# search for the first line that starts with "version" in build.gradle.kts
# get the value in the quotes
VERSION=$(grep "^version = " build.gradle.kts | sed -n 's/version = "\(.*\)"/\1/p')

# increment the version number
NEW_VERSION=$(echo $VERSION | awk -F. '{$NF = $NF + 1;} 1' | sed 's/ /./g')

#if NEW_VERSION is not empty, replace the version in build.gradle.kts
if [ -n "$NEW_VERSION" ]; then
  sed -i '' "s/version = \"$VERSION\"/version = \"$NEW_VERSION\"/" build.gradle.kts
  echo "Version bumped to $NEW_VERSION"
fi
