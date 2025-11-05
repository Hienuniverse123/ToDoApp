plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.todoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nhom2.todoapp"
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    // 2. Xóa phiên bản khỏi các thư viện Firebase (BOM sẽ tự quản lý)
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")

    //Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    //material
    implementation ("com.google.android.material:material:1.12.0")
    //facebook sdk
    implementation("com.facebook.android:facebook-login:16.3.0")
}
