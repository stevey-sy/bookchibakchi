import java.util.Properties
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
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

//            buildConfigField("String","NAVER_CLIENT_ID",
//                ""+localProperties["NAVER_CLIENT_ID"]+"")
//            buildConfigField("String","NAVER_CLIENT_SECRET",
//                localProperties["NAVER_CLIENT_SECRET"].toString()
//            )
//            buildConfigField("String", "NAVER_CLIENT_ID", "\"${localProperties["NAVER_CLIENT_ID"] ?: ""}\"")
//            buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${localProperties["NAVER_CLIENT_SECRET"] ?: ""}\"")
        }
        release {
//            buildConfigField("String","NAVER_CLIENT_ID",
//                localProperties["NAVER_CLIENT_ID"].toString()
//            )
//            buildConfigField("String","NAVER_CLIENT_SECRET",
//                localProperties["NAVER_CLIENT_SECRET"].toString()
//            )
//            buildConfigField("String", "NAVER_CLIENT_ID", "\"${localProperties["NAVER_CLIENT_ID"] ?: ""}\"")
//            buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${localProperties["NAVER_CLIENT_SECRET"] ?: ""}\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

//fun getApiKey(propertyKey: String): String {
//    return gradleLocalProperties("").getProperty(propertyKey)
//}

// android 블록 외부에 local.properties 파일 읽기 추가
//val localProperties = Properties()
//val localPropertiesFile = rootProject.file("local.properties")
//if (localPropertiesFile.exists()) {
//    localPropertiesFile.reader(Charsets.UTF_8).use { reader ->
//        localProperties.load(reader)
//    }
//}

dependencies {

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

    // 네트워크 통신용 Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    // 코루틴
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)

    implementation (libs.logging.interceptor)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}