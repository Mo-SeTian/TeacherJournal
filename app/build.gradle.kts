plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.teacher.journal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.teacher.journal"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

        signingConfigs {
        create("ci") {
            // 环境变量优先（CI），回退到 keystore.properties 文件（本地）
            val envStoreFile = System.getenv("KEYSTORE_FILE")
            if (envStoreFile != null) {
                storeFile = file(envStoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS") ?: "teacherjournal"
                keyPassword = System.getenv("KEY_PASSWORD")
            } else {
                val propsFile = rootProject.file("keystore.properties")
                if (propsFile.exists()) {
                    // 手动解析 properties 文件，避免 java.util.Properties 在 .kts 中受限
                    val lines = propsFile.readLines()
                    val map = lines.filter { it.contains("=") && !it.trimStart().startsWith("#") }
                        .associate { 
                            val (k, v) = it.split("=", limit = 2)
                            k.trim() to v.trim()
                        }
                    storeFile = rootProject.file(map["storeFile"] ?: "keystore.properties.jks")
                    storePassword = map["storePassword"] ?: "android"
                    keyAlias = map["keyAlias"] ?: "teacherjournal"
                    keyPassword = map["keyPassword"] ?: "android"
                }
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("ci")
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
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))

    // Compose UI
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // Activity & Lifecycle
    implementation(libs.androidx.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Core
    implementation(libs.androidx.core.ktx)

    // DataStore
    implementation(libs.datastore.preferences)

    // Debug
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
