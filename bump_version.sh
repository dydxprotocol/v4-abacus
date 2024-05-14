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

# Remove the warning step about ./gradlew :generateDummyFramework, since on iOS we use CocoaPods to 
# download the source code and build locally.
PODSPEC_FILE="v4_abacus.podspec"

# replace the line "if !Dir.exist?('build/cocoapods/framework/Abacus.framework') || Dir.empty?('build/cocoapods/framework/Abacus.framework')" with "if false"
sed -i '' "s/if !Dir.exist?('build\/cocoapods\/framework\/Abacus.framework') || Dir.empty?('build\/cocoapods\/framework\/Abacus.framework')/if false/" $PODSPEC_FILE

cd integration/iOS
pod install
