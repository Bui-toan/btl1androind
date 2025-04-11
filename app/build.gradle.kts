    plugins {
        alias(libs.plugins.android.application)
    }

    android {
        namespace = "com.example.btl1"
        compileSdk = 35

        defaultConfig {
            applicationId = "com.example.btl1"
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

        buildFeatures {
            viewBinding = true
        }
    }

    dependencies {
        implementation(libs.core)
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation("androidx.media:media:1.6.0")
        implementation("androidx.cardview:cardview:1.0.0")

        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
    }
