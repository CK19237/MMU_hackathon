import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.colleen.s36349879.medtrack"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }

    defaultConfig {
        applicationId = "com.colleen.s36349879.medtrack"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val apiKey = properties.getProperty("GEMINI_API_KEY") ?: "NO_KEY"

        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.generativeai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("com.google.code.gson:gson:2.13.2")
    val nav_version = "2.8.9"
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:$room_version")
    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp("androidx.room:room-compiler:$room_version")
    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:$room_version")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")
    // Standard Retrofit Library
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

// Converter (choose one based on your JSON library)
// For Gson:
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

// For kotlinx.serialization (Modern Kotlin standard):
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
}