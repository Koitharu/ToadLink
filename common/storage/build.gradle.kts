plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "org.koitharu.toadlink.storage"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(project(":common:core"))
    implementation(libs.room)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.dagger.hilt)
    ksp(libs.dagger.hilt.compiler)
}