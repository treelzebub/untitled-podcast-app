import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    kotlin("kapt")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.sqldelight)
    id("com.google.devtools.ksp")
    alias(libs.plugins.compose.compiler)
}

sqldelight {
    databases {
        create("Database") {
            dialect(libs.sqldelight.sql335)
            packageName.set("net.treelzebub.podcasts")
        }
    }
}

kapt {
    correctErrorTypes = true
}

kotlin {
    jvmToolchain(17)
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

android {
    val localProps = gradleLocalProperties(rootDir, providers)
    fun local(key: String): String = localProps.getProperty(key)

    buildFeatures.buildConfig = true

    namespace = "net.treelzebub.podcasts"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "net.treelzebub.podcasts"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = compileSdk
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val apiKeyPodcastIndex: String = local("API_KEY_PODCAST_INDEX")
        val apiSecretPodcastIndex: String = local("API_SECRET_PODCAST_INDEX")
        buildConfigField("String", "API_KEY_PODCAST_INDEX", apiKeyPodcastIndex)
        buildConfigField("String", "API_SECRET_PODCAST_INDEX", apiSecretPodcastIndex)
        buildConfigField("String", "USER_AGENT_PODCAST_INDEX", "\"UntitledPodcastApp/$versionName\"")
    }

    signingConfigs {
//        getByName("debug") {
//            keyAlias = "debug"
//            keyPassword = "debug"
//            storeFile = file("debug.jks")
//            storePassword = "debug"
//        }
        create("release") {
            enableV1Signing = false
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
            storeFile = file(local("PODCAST_STORE_PATH"))
            storePassword = local("PODCAST_STORE_PW")
            keyAlias = local("PODCAST_KEY_ALIAS")
            keyPassword = local("PODCAST_KEY_PW")
        }
    }

    buildTypes {
        debug {
            name
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compiler.html#compose-compiler-options-dsl
composeCompiler {}

dependencies {
    implementation(libs.coroutines.android)
    implementation(platform(libs.compose.bom))
    implementation(libs.work)
    implementation(libs.datastore.prefs)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)

    implementation(libs.sqldelight.driver)
    implementation(libs.sqldelight.coroutines)

    implementation(libs.timber)

    implementation(libs.rssparser)

    implementation(libs.androidx.core)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    ksp(libs.compose.destinations)
    implementation(libs.compose.destinations.animations.core)

    implementation(libs.exoplayer)
    implementation(libs.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)

    implementation(libs.androidx.graphics.core)
    implementation(libs.coil)
    implementation(libs.coil.compose)

    debugImplementation(libs.debug.compose.ui.tooling)
    debugImplementation(libs.debug.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.hilt.test) // For Robolectric
    testImplementation(libs.sqldelight.test)
    testImplementation(libs.slf4j) // to shut SqlDelight logs up under test

//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//    androidTestImplementation(platform("androidx.compose:compose-bom:$compose_bom"))
//    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
//    androidTestImplementation("androidx.work:work-testing:$work")
}
