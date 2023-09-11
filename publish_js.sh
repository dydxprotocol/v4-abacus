#!/bin/sh

./gradlew clean
./gradlew assembleJsPackage
cp "LICENSE" "build/packages/js/LICENSE"
./gradlew packJsPackage
./gradlew publishJsPackageToNpmjsRegistry
