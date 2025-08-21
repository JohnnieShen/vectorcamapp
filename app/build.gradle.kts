import java.util.Properties

val secretsProperties = Properties()
val secretsFile = rootProject.file("secrets.properties")
if (secretsFile.exists()) {
    secretsFile.inputStream().use { secretsProperties.load(it) }
}

// Region configuration
val region = project.findProperty("region")?.toString()?.lowercase() ?: "default"
println("✅ Building VectorCam for region: $region")

fun getRegionBasedVersionCode(): Int {
    return when (region) {
        "colombia" -> 1003
        "uganda" -> 2000
        else -> {
            println("⚠️ Unknown region '$region', using default version code")
            3000
        }
    }
}

fun getRegionBasedVersionName(): String {
    return when (region) {
        "colombia" -> "1.0.3"
        "uganda" -> "1.0.0"
        else -> {
            println("⚠️ Unknown region '$region', using default version name")
            "1.0.0"
        }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.sentry.android.gradle)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.vci.vectorcamapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vci.vectorcamapp"
        minSdk = 29
        targetSdk = 35

        versionCode = getRegionBasedVersionCode()
        versionName = getRegionBasedVersionName()

        testInstrumentationRunner = "com.vci.vectorcamapp.HiltTestRunner"

        buildConfigField("String", "POSTHOG_API_KEY", "\"${secretsProperties["POSTHOG_API_KEY"]}\"")
        buildConfigField("String", "POSTHOG_HOST", "\"${secretsProperties["POSTHOG_HOST"]}\"")

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            buildConfigField("String", "BASE_URL", "\"https://test.api.vectorcam.org/\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )

            buildConfigField("String", "BASE_URL", "\"https://api.vectorcam.org/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    androidResources {
        noCompress += "tflite"
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {
    // Core Android Libraries
    implementation(libs.androidx.core.ktx) // Kotlin extensions for Android core libraries
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle-aware components
    implementation(libs.androidx.activity.compose) // Compose integration with activities

    // Jetpack Window Manager Library
    implementation(libs.androidx.window)
    implementation(libs.androidx.window.testing)

    // Jetpack Compose Dependencies
    implementation(platform(libs.androidx.compose.bom)) // BOM for Compose version alignment
    implementation(libs.androidx.ui) // Compose UI components
    implementation(libs.androidx.ui.graphics) // Compose graphics library
    implementation(libs.androidx.ui.tooling.preview) // Preview support for Compose
    implementation(libs.androidx.material3) // Material Design 3 components
    implementation(libs.androidx.ui.text.google.fonts) // Google Fonts support for Compose

    // Navigation Libraries
    implementation(libs.androidx.navigation.compose) // Jetpack Compose navigation
    implementation(libs.androidx.navigation.fragment) // Navigation for Fragments
    implementation(libs.androidx.navigation.ui) // Navigation UI helpers
    implementation(libs.androidx.navigation.dynamic.features.fragment) // Feature module support for Fragments
    androidTestImplementation(libs.androidx.navigation.testing) // Testing navigation

    // CameraX Dependencies
    implementation(libs.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle) // CameraX Lifecycle library
    implementation(libs.androidx.camera.view) // CameraX View class
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.mlkit.vision) // CameraX ML Kit Vision Integration
    implementation(libs.androidx.camera.extensions) // CameraX Extensions library

    // Ktor (Networking) Dependencies
    implementation(libs.ktor.client.android) // Android client for Ktor
    implementation(libs.ktor.client.json) // JSON plugin for Ktor
    implementation(libs.ktor.client.logging) // Logging plugin for Ktor
    implementation(libs.ktor.client.core) // Core Ktor client
    implementation(libs.ktor.client.cio) // CIO engine for Ktor
    implementation(libs.ktor.client.content.negotiation) // Content negotiation plugin
    implementation(libs.ktor.serialization.kotlinx.json) // Kotlinx JSON serialization for Ktor

    // Dagger Hilt Dependencies
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    ksp(libs.hilt.android.compiler)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    // Open CV Library
    implementation(libs.opencv)

    // LiteRT Library
    implementation(libs.litert)
    implementation(libs.litert.gpu)
    implementation(libs.litert.gpu.api)
    implementation(libs.litert.support)

    // Room Database Dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)

    // Google Play Services Dependencies
    implementation(libs.play.services.location) // Location Services Library

    // Coil Async Image Rendering Library
    implementation(libs.coil.compose)

    // MLKit Text Recognition Library
    implementation(libs.text.recognition)

    // Proto Data Store Library
    implementation(libs.androidx.datastore)

    // Work Manager Library
    implementation(libs.androidx.work.runtime.ktx)

    // PostHog AnalyticsLibrary
    implementation(libs.posthog.android)

    // JSON Serialization Library
    implementation(libs.kotlinx.serialization.json) // Kotlinx JSON serialization library

    // TUS Library
    implementation(libs.tus.android.client)
    implementation(libs.tus.java.client)

    // Testing Dependencies
    testImplementation(libs.junit) // JUnit for unit tests
    testImplementation(libs.truth) // Google truth library for assertions
    testImplementation(libs.kotlinx.coroutines.test) // Kotlin coroutines test library
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit) // AndroidX JUnit test library
    androidTestImplementation(libs.androidx.espresso.core) // Espresso for UI testing
    androidTestImplementation(libs.truth) // Google truth library for assertions
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Compose testing BOM
    androidTestImplementation(libs.androidx.ui.test.junit4) // Compose JUnit testing
    debugImplementation(libs.androidx.ui.tooling) // Debugging tools for Compose
    debugImplementation(libs.androidx.ui.test.manifest) // Debugging Compose manifest tests
}

sentry {
    org.set("vectorcam")
    projectName.set("android")
    includeSourceContext.set(true)
    includeProguardMapping.set(true)
    autoUploadProguardMapping.set(true)

    ignoredBuildTypes.set(setOf("debug"))
}
