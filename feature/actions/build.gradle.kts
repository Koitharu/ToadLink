plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.dagger.hilt)
}


android {
    namespace = "org.koitharu.toadlink.actions"
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
    kotlin {
        compilerOptions {
            optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
            optIn.add("kotlinx.coroutines.FlowPreview")
            optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
            optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":common:core"))
    implementation(project(":common:ui"))
    implementation(project(":common:storage"))
    implementation(project(":common:ssh-client"))

    implementation(libs.dagger.hilt)
    implementation(libs.androidx.compose.runtime)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.viewmodel.compose)
}