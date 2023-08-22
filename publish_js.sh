#!/bin/sh

./gradlew clean
./gradlew assembleJsPackage
./gradlew packJsPackage
./gradlew publishJsPackageToNpmjsRegistry
