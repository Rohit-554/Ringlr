import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    //alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    //id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    /*cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "shared"
            isStatic = true
        }
    }*/
    
    sourceSets {
        androidMain.dependencies {
            implementation (libs.koin.android)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
    }
}

android {
    namespace = "io.jadu.ringlr"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}