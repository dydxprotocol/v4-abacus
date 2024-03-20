import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

buildscript {
    val agp_version by extra("7.2.2")
    repositories {
        //gradlePluginPortal()
        //google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        classpath("com.android.tools.build:gradle:$agp_version")
    }
}

plugins {
    kotlin("multiplatform") version "1.9.10"
    kotlin("native.cocoapods") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("maven-publish")
    id("dev.petuska.npm.publish") version "3.1.0"
    id("com.diffplug.spotless") version "6.25.0"
}

allprojects {
    apply(plugin = "com.diffplug.spotless")
    spotless {
        kotlin {
            target("src/**/*.kt")
            targetExclude(".idea/")
            ktlint("1.2.1")
                .editorConfigOverride(
                    mapOf(
                        "max_line_length" to "off",
                        "filename" to "off",
                        "pascal_case_package" to "disabled",
                        "ktlint_standard_enum-entry-name-case" to "disabled",
                        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                        "ktlint_standard_no-semi" to "disabled",
                        "ktlint_standard_backing-property-naming" to "disabled",
                        "ktlint_standard_filename" to "disabled",
                    )
                )

        }
    }
    repositories {
        google()
        mavenCentral()
    }
}

group = "exchange.dydx.abacus"
version = "1.7.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    //android()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.moduleName = "abacus"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        moduleName = "abacusjs"
        browser {
            testTask {
                useMocha {
                    timeout = "10s"
                }
            }
        }
        generateTypeScriptDefinitions()
        binaries.library()
        browser()
    }

    val xcf = XCFramework()

    iosArm64 {
        binaries.framework {
            baseName = "abacus"
            xcf.add(this)
        }
    }

    iosX64 {
        binaries.framework {
            baseName = "abacus"
            xcf.add(this)
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = "abacus"
            xcf.add(this)
        }
    }


    sourceSets {
        val ktorVersion = "2.1.1"
        val napierVersion = "2.6.1"
        all {
            languageSettings.apply {
                optIn("kotlin.js.ExperimentalJsExport")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                implementation("io.github.aakira:napier:$napierVersion")
                implementation("co.touchlab:stately-common:1.2.0")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("com.ionspin.kotlin:bignum:0.3.8")
                implementation("tz.co.asoft:kollections-interoperable:2.0.16")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting

        val androidMain by creating {
            dependsOn(commonMain)
        }
        val jvmMain by getting {
            dependsOn(androidMain)
        }
        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }

    tasks.wrapper {
        gradleVersion = "7.5.1"
        distributionType = Wrapper.DistributionType.ALL
    }


    /*
    tasks.named<KotlinJsCompile>("compileKotlinJs").configure {
        kotlinOptions.moduleKind = "plain"
    }

     */

    cocoapods {
        // Required properties
        // Specify the required Pod version here. Otherwise, the Gradle project version is used.
        // version = "1.0"
        summary = "Shared front-end and mobile logic written in Kotlin"
        homepage = "https://github.com/dydxprotocol/v4-abacus"
        source = "{ :git => 'git@github.com/dydxprotocol/v4-abacus.git' }"

        // Optional properties
        // Configure the Pod name here instead of changing the Gradle project name
        //name = "MyCocoaPod"

        framework {
            // Required properties
            // Framework name configuration. Use this property instead of deprecated 'frameworkName'
            baseName = "Abacus"

            // Optional properties
            // Dynamic framework support
            isStatic = false
            // Dependency export
            //export(project(":anotherKMMModule"))
            //transitiveExport = false // This is default.
            // Bitcode embedding
            //embedBitcode(BITCODE)
        }
        // Maps custom Xcode configuration to NativeBuildType
        //xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        //xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }
}

npmPublish {
    organization.set("dydxprotocol")
    readme.set(rootDir.resolve("README.md"))
    packages {
        named("js") {
            packageJson {
                license.set("BSL-1.1")
            }
        }
    }
    registries {
        register("npmjs") {
            uri.set(uri("https://registry.npmjs.org")) //
            val npm_token = System.getenv("npm_token")
            authToken.set(npm_token)
        }
    }
}

//
// For Android app
//

publishing {
    repositories {
        maven {
            val github_username = System.getenv("github_username")
            val github_token = System.getenv("github_token")
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/dydxprotocol/v4-abacus")
            credentials {
                username = github_username
                password = github_token
            }
        }
    }
}
