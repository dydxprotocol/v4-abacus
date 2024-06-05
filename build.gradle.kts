import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.js.ir.JsIrBinary

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    }
}

plugins {
    kotlin("multiplatform") version "1.9.24"
    kotlin("native.cocoapods") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    id("maven-publish")
    id("dev.petuska.npm.publish") version "3.4.2"
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version("1.23.3")
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" // needs to be in-sync with Kotlin version. Version before the dash is the compatible Kotlin version.
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
version = "1.7.53"

repositories {
    google()
    mavenCentral()
}

kotlin {
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
        browser()
        generateTypeScriptDefinitions()
        binaries.library()

        binaries.withType<JsIrBinary>().all {
            linkTask.configure {
                kotlinOptions {
                    sourceMap = true
                    sourceMapEmbedSources = "always"
                }
            }
        }

    }

    val xcf = XCFramework()

    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    iosTargets.forEach {
        it.binaries.framework {
            baseName = "abacus"
            xcf.add(this)
        }
    }

    sourceSets {
        val ktorVersion = "2.1.1"
        all {
            // Since commonMain needs the opt-in, all dependent sets also need it.
            languageSettings.apply {
                optIn("kotlin.js.ExperimentalJsExport")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                implementation("io.ktor:ktor-http:$ktorVersion")
                implementation("com.ionspin.kotlin:bignum:0.3.8")
                implementation("tz.co.asoft:kollections-interoperable:2.0.16")
                implementation("me.tatarka.inject:kotlin-inject-runtime:0.6.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }
        val jsMain by getting
        val jvmMain by getting
        val jvmTest by getting
        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }

    dependencies {
        // KSP will eventually have better multiplatform support and we'll be able to simply have
        // `ksp libs.kotlinInject.compiler` in the dependencies block of each source set
        // https://github.com/google/ksp/pull/1021
        add("kspJvm", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.6.3")
        add("kspJs", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.6.3")
        add("kspIosArm64", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.6.3")
        add("kspIosSimulatorArm64", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.6.3")
    }

    tasks.wrapper {
        gradleVersion = "7.5.1"
        distributionType = Wrapper.DistributionType.ALL
    }

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

detekt {
    toolVersion = "1.23.3"
    config.setFrom(file("detekt.yml"))
    baseline = file("detekt-baseline.xml")
    buildUponDefaultConfig = true
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
// JVM publishing
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
