<p align="center"><img src="https://dydx.exchange/icon.svg?" width="256" /></p>

<h1 align="center">dYdX Chain Abacus</h1>

<div align="center">
  <a href='https://www.npmjs.com/package/@dydxprotocol/v4-abacus'>
    <img src='https://img.shields.io/npm/v/@dydxprotocol/v4-abacus.svg' alt='NPM'/>
  </a>
  <a href='https://github.com/dydxprotocol/v4-chain/blob/main/v4-abacus/LICENSE'>
    <img src='https://img.shields.io/badge/License-AGPL_v3-blue.svg' alt='License' />
  </a>
</div>

Shared front-end and mobile logic written in Kotlin Multiplatform (https://kotlinlang.org/docs/multiplatform.html).

The library generates Swift framework for iOS, JVM library for Android, and Javascript code for Web.

![giphy](https://user-images.githubusercontent.com/102453770/175617972-a2a727fc-b154-4770-9b39-c6372d7777ce.gif)

# Install Java 11

https://www.oracle.com/java/technologies/downloads/#java11

# Documentation

[API Documentation](docs/Abacus.md)

# iOS

Abacus uses Cocoapods to integrate with iOS project. The gradle configuration contains the steps needed to generate the .podspec file. Run

> ./gradlew podspec

to generate abacus.podspec. Configure your iOS project (https://github.com/dydxprotocol/native-ios) to import abacus.podspec.

You can also build the Abacus for iOS by running:

> ./gradlew assembleXCFramework

This generates the iOS framework in **build/XCFrameworks** folder.

Debugging on iOS directly from XCode is possible with a plugin (https://github.com/touchlab/xcode-kotlin)

# Android

Abacus builds and pushes the JVM target as a Github package with the following command:

> ./publish_android.sh

The Android app (https://github.com/dydxprotocol/native-android) has the Gradle build step to pull the Abacus target from GithubPackage.

# v4-web

**If you are using v4-abacus with the v4-web repo, follow local development instructions [here](https://github.com/dydxprotocol/v4-web?tab=readme-ov-file#local-abacus-development).**

# Other JavaScript / TypeScript

Abacus generates a Javascript / Typescript UMD module with the following command:

> ./gradlew assembleJsPackage

This outputs into **build/packages/js**.

A local npm package can be built with:

> ./gradlew packJsPackage

A tarball of the package should be created in **build/packages** and you can install it to your project for local testing:

> npm install PATH_TO_TARBALL

# Publishing to NPM

Abacus is published to an npm library (https://www.npmjs.com/package/@dydxprotocol/v4-abacus).

# Unit Tests

Shared code should have unit tests written in Kotlin residing in the src/CommonTest directory. Run the tests with the following command

> ./gradlew jvmtest

# Integration Tests

Integration tests can be written to call Abacus from non-Kotlin code (i.e., Swift, JS). Sample integration projects can be found in the **integration** directory.

# Version Bump

> ./bump_version.sh

# Auto-lint

Enable pre-commit to auto-lint/auto-format your changes before git commit:

> brew install pre-commit
> pre-commit install

# How to use

```
// create a state machine
val stateMachine = PerpTradingStateMachine()

// send socket payload to the state machine and get the state
// the param is the complete socket text
val state = stateMachine.socket(payloadText)

// See src/commonTest/kotlin/exchange.dydx.abacus/PerpV3Tests.kt for testing code
```

# Structure

Misc:
- Utils
- Protocols

state (top state)
- app -> AppStateMachine (contains network logic)
- modal -> StateMachine (contains business logic)
- changes -> Changes (utilities to identify which part of the state has changed)

processing:

step 1: processor (dynamic objects - dictionaries, list, not typed)
- markets
  - orderbook
  - trades
  - funding
- asset (referenced from markets, such as icon, url etc)
- wallet (user info)
  - account
    - subaccount
      - assetPositions
      - openPositions
      - orders
      - fills
      - transfers
      - historicalPnl
- configs (from Veronica mostly)

step 2 calculator (dynamic)
- market (summary info)
- account (step 3)
  - subaccount
    - 3.1 calculate positon notionalTotal/valueTotal etc
    - 3.2 calculate account equity etc, leverage, margin usage, buyingpower
    - 3.3 calcualte position levereage, buyingpower
- account transformer (step 2)
  - calculate postOrder and postAllOrderStates for account (total from trade input)
and positions (size from trade input)
- input (step 1)
  - trade input
    - size (size, usdcSize, leverage)
  - transfer input (not complete)

step 3 validator (from postOrder and postAllOrders states)
- trade
- transfer

step 4 output (structs, typed data)
- converts dynamic data to typed

step 5 responses
- Construct response object from output

# CommonTest

test (supporting classes, mostly mocks)
utils (just utilities)

AppStateMachine (app)
- StateMachine (payload and validation folder)

payload (test StateMachine payload and interaction)
- API -> expected state
validation (separated from payload, to target validation tests)

app (test AppStateMachine IO requests)
