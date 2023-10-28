plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("app.cash.sqldelight") version "2.0.0"
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("net.treelzebub.podcasts")
        }
    }
}


android {
    namespace = "net.treelzebub.podcasts"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.treelzebub.podcasts"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // todo move to version catalog
    val lifecycle = "2.6.2"
    val compose_bom = "2023.10.01"
    val compose = "1.5.4"
    val exoplayer = "1.1.1"
    val coil = "2.4.0"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:$compose_bom"))
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.ui:ui-graphics:$compose")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose")
    implementation("androidx.compose.material3:material3:1.1.2")

    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("app.cash.sqldelight:android-driver:2.0.0")
    implementation("com.prof18.rssparser:rssparser:6.0.3")

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