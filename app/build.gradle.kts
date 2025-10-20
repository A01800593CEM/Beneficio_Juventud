import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "mx.itesm.beneficiojuventud"
    compileSdk = 36

    defaultConfig {
        applicationId = "mx.itesm.beneficiojuventud"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "META-INF/DEPENDENCIES",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.SF",
                "META-INF/*.DSA",
                "META-INF/*.RSA"
            )
        }
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.ui.geometry)
    implementation(libs.androidx.runtime.saveable)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.runtime.saveable)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.ui.text)
    implementation(libs.ui)
    implementation(libs.firebase.messaging)
    implementation(libs.litert.support.api)
    implementation(libs.foundation.layout)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    // 🔹 Core y Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // ADD: módulos granulares seguros (solo exponen APIs; no rompen nada si no se usan)
    implementation(libs.androidx.compose.foundation)          // ADD
    implementation(libs.androidx.material.icons.core)          // ADD
    implementation(libs.androidx.runtime.saveable)             // ADD
    implementation(libs.androidx.navigation.runtime.ktx)       // ADD

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.compose.material:material:1.6.8")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // 🔹 Amplify
    implementation(libs.amplify.core)
    implementation(libs.amplify.auth)
    implementation(libs.amplify.datastore)
    implementation(libs.amplify.api)
    implementation(libs.amplify.storage)

    // 🔹 Firebase
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.appdistribution.gradle)

    // 🔹 Retrofit
    implementation(libs.retrofit.lib)
    implementation(libs.converter.lib)

    // 🔹 CameraX y ML Kit
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.zxing:core:3.5.3")

    // 🔹 Permisos en Compose
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // 🔹 Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // 🔹 Google Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.maps.android:maps-ktx:5.1.1")
    implementation("com.google.maps.android:maps-utils-ktx:5.1.1")

    // 🔹 Seguridad (EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // 🔹 Otras librerías del proyecto
    implementation(libs.vico.core)
    implementation(libs.vico.compose)

    // CameraX for QR scanning
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // 🔹 Desugaring (para funciones modernas de Java)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.1")
    // RoomDB Dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // OPTIONAL: LiteRT / TFLite (solo descomenta si realmente lo usas para ML local)
    // implementation(libs.litert.support.api)                 // ADD (opcional)

    // 🔹 Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 🔹 Testing adicional para ViewModels y Coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // 🔹 Testing para Compose UI
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")

    // 🔹 Espresso adicional para navegación y acciones
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")

    // 🔹 Testing de Room
    testImplementation("androidx.room:room-testing:2.6.1")

    // 🔹 Testing con Retrofit y APIs
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
}
