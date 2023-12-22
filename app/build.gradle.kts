import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    kotlin("kapt")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("app.cash.sqldelight") version "2.0.0"
    id("com.google.devtools.ksp")
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("net.treelzebub.podcasts")
        }
    }
}

kapt {
    correctErrorTypes = true
}

kotlin {
    jvmToolchain(17)
}

android {
    buildFeatures.buildConfig = true

    namespace = "net.treelzebub.podcasts"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.treelzebub.podcasts"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val apiKeyPodcastIndex: String = gradleLocalProperties(rootDir).getProperty("API_KEY_PODCAST_INDEX")
        val apiSecretPodcastIndex: String = gradleLocalProperties(rootDir).getProperty("API_SECRET_PODCAST_INDEX")
        buildConfigField("String", "API_KEY_PODCAST_INDEX", apiKeyPodcastIndex)
        buildConfigField("String", "API_SECRET_PODCAST_INDEX", apiSecretPodcastIndex)
        buildConfigField("String", "USER_AGENT_PODCAST_INDEX", "\"UntitledPodcastApp/$versionName\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // todo move to version catalog
    val retrofit = "2.9.0"
    val sqldelight = "2.0.0"
    val hilt = "2.48.1"
    val hiltAndroidX = "1.1.0"
    val lifecycle = "2.6.2"
    val compose_bom = "2023.10.01"
    val compose = "1.5.4"
    val destinations = "1.9.54"
    val exoplayer = "1.1.1"
    val coil = "2.4.0"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.google.dagger:hilt-android:$hilt")
    kapt("com.google.dagger:hilt-android-compiler:$hilt")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltAndroidX")
//    implementation("androidx.hilt:hilt-compiler:$hiltAndroidX")

    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.squareup.retrofit2:retrofit:$retrofit")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofit")

    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("app.cash.sqldelight:android-driver:$sqldelight")
    implementation("app.cash.sqldelight:coroutines-extensions:$sqldelight")
    implementation("com.prof18.rssparser:rssparser:6.0.3")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:$compose_bom"))
    implementation("androidx.compose.foundation:foundation:$compose")
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.ui:ui-graphics:$compose")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose")
    implementation("androidx.compose.material3:material3:1.1.2")

    implementation("io.github.raamcosta.compose-destinations:animations-core:$destinations")
    ksp("io.github.raamcosta.compose-destinations:ksp:$destinations")

    implementation("androidx.media3:media3-exoplayer:$exoplayer")
    implementation("androidx.media3:media3-exoplayer-dash:$exoplayer")
    implementation("androidx.media3:media3-ui:$exoplayer")
    implementation("io.coil-kt:coil:$coil")
    implementation("io.coil-kt:coil-compose:$coil")

    debugImplementation("androidx.compose.ui:ui-tooling:$compose")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:$compose_bom"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}