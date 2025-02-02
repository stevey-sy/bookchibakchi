import java.util.Properties
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.bookchigibakchigi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bookchigibakchigi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
//        buildConfigField("String", "API_KEY", getApiKey("API_KEY"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "NAVER_CLIENT_ID", gradleLocalProperties(rootDir, providers).getProperty("NAVER_CLIENT_ID"))
            buildConfigField("String", "NAVER_CLIENT_SECRET", gradleLocalProperties(rootDir, providers).getProperty("NAVER_CLIENT_SECRET"))
            buildConfigField("String", "ALADIN_TTB_KEY", gradleLocalProperties(rootDir, providers).getProperty("ALADIN_TTB_KEY"))
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "NAVER_CLIENT_ID", gradleLocalProperties(rootDir, providers).getProperty("NAVER_CLIENT_ID"))
            buildConfigField("String", "NAVER_CLIENT_SECRET", gradleLocalProperties(rootDir, providers).getProperty("NAVER_CLIENT_SECRET"))
            buildConfigField("String", "ALADIN_TTB_KEY", gradleLocalProperties(rootDir, providers).getProperty("ALADIN_TTB_KEY"))
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
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    val room_version = "2.6.1"
    implementation(libs.androidx.room.runtime)
    kapt (libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.sdp.android)
    implementation(libs.lottie)


    // 네트워크 통신용 Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    // 코루틴
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)

    implementation (libs.logging.interceptor)

    // glide
    implementation (libs.github.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // fab
    implementation(libs.fab)
}