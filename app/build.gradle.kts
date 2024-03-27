import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    kotlin("kapt")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("app.cash.sqldelight") version "2.0.1"
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

        val localProps = gradleLocalProperties(rootDir, providers)
        val apiKeyPodcastIndex: String = localProps.getProperty("API_KEY_PODCAST_INDEX")
        val apiSecretPodcastIndex: String = localProps.getProperty("API_SECRET_PODCAST_INDEX")
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
    val sqldelight = "2.0.1"
    val hilt = "2.51"
    val hiltAndroidX = "1.2.0"
    val lifecycle = "2.7.0"
    val compose_bom = "2024.03.00"
    val destinations = "1.10.0"
    val exoplayer = "1.3.0"
    val coil = "2.4.0"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(platform("androidx.compose:compose-bom:$compose_bom"))

    implementation("com.google.dagger:hilt-android:$hilt")
    kapt("com.google.dagger:hilt-android-compiler:$hilt")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltAndroidX")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.squareup.retrofit2:retrofit:$retrofit")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofit")

    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("app.cash.sqldelight:android-driver:$sqldelight")
    implementation("app.cash.sqldelight:coroutines-extensions:$sqldelight")
    implementation("com.prof18.rssparser:rssparser:6.0.3")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")

    implementation("io.github.raamcosta.compose-destinations:animations-core:$destinations")
    ksp("io.github.raamcosta.compose-destinations:ksp:$destinations")

    implementation("androidx.media3:media3-exoplayer:$exoplayer")
    implementation("androidx.media3:media3-exoplayer-dash:$exoplayer")
    implementation("androidx.media3:media3-ui:$exoplayer")
    implementation("androidx.media3:media3-session:$exoplayer")

    implementation("io.coil-kt:coil:$coil")
    implementation("io.coil-kt:coil-compose:$coil")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
    // For Robolectric tests.
    testImplementation("com.google.dagger:hilt-android-testing:$hilt")
    // ...with Kotlin.
    kaptTest("com.google.dagger:hilt-android-compiler:2.51")
    // ...with Java.
    testAnnotationProcessor("com.google.dagger:hilt-android-compiler:$hilt")
    testImplementation("org.robolectric:robolectric:4.11.1")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:$compose_bom"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}