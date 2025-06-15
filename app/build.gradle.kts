plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mapubication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mapubication"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    // Dependencias básicas
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Navigation Drawer y compoqnentes de Material Design
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    implementation("androidx.navigation:navigation-fragment:2.7.5")

    // Geocoder mejorado (opcional)
    implementation("com.google.android.libraries.places:places:3.2.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}