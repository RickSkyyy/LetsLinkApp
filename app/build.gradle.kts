plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}


//configurations.all {
//    resolutionStrategy {
//        force("org.jetbrains.kotlinx:kotlinx-io-jvm:0.3.1")
//        force("org.jetbrains.kotlinx:kotlinx-io-core:0.3.1")
//    }
//}

android {
    namespace = "com.example.letslink"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.letslink"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ViewPager / CoordinatorLayout / RecyclerView
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.viewpager)
    implementation(libs.material)

    // Google Play services
    implementation(libs.play.services.maps)

    // Extras
    implementation(libs.glide)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.material3)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.volley)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Other UI
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation("com.google.android.material:material:1.13.0")

    // Hashing
    implementation(platform("org.kotlincrypto.hash:bom:0.8.0"))
    implementation("org.kotlincrypto.hash:md")
    implementation("org.kotlincrypto.hash:sha1")
    implementation("org.kotlincrypto.hash:sha2")
    implementation("org.kotlincrypto.hash:sha3")
    implementation("org.kotlincrypto.hash:blake2")
    implementation("com.google.android.material:material:1.12.0")

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.retrofit2.kotlin.coroutines.adapter)

    // Gson library
    implementation("com.google.code.gson:gson:2.10.1")

    // KotlinX Metadata
    implementation(libs.kotlinx.metadata.jvm)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.google.signin)
    implementation(libs.firebase.messaging)
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.android.gms:play-services-auth:20.6.0")

//    // KotlinX IO (force-stable version)
//    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.5.5")
//    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:0.5.5")

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.core.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
 //biometrics
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    //glide
    implementation(libs.glide.v4160)

    //pdf reader
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2") {
        exclude(group = "com.android.support")
    }
}
