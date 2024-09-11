#!/bin/sh

# search for the first line that starts with "version" in build.gradle.kts
# get the value in the quotes
VERSION=$(grep "^version = " build.gradle.kts | sed -n 's/version = "\(.*\)"/\1/p')

echo "Current version is $VERSION. Enter new version (or press enter to skip):"
read NEW_VERSION

#if NEW_VERSION is not empty, replace the version in build.gradle.kts
if [ -n "$NEW_VERSION" ]; then
  sed -i '' "s/version = \"$VERSION\"/version = \"$NEW_VERSION\"/" build.gradle.kts
  echo "Version bumped to $NEW_VERSION"
fi

#
# Update the version in the podspec file
#
./gradlew podspec

cd integration/iOS
pod install
