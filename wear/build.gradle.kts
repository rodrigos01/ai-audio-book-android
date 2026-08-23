import java.util.Properties

// NOTE: the `com.google.gms.google-services` plugin is intentionally NOT applied yet. It requires
// a `wear/google-services.json` for a Firebase Android app registered under applicationId
// "com.rodrigos01.aiaudiobook.wear" (same Firebase project as the phone app) — a manual, one-time
// step in the Firebase console. Once that file is added (gitignored, distributed out-of-band like
// the phone's `app/google-services.json`), re-add the plugin alias below and this module's
// dependency on it.
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.rodrigos01.aiaudiobook.wear"
    compileSdk = 37

    signingConfigs {
        create("release") {
            storeFile = file("../app/ai-audio-book-keystore")
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    defaultConfig {
        applicationId = "com.rodrigos01.aiaudiobook.wear"
        // Wear Compose Material3 / Horologist require Wear OS 3+ (API 30).
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "SERVER_URL", "\"http://10.0.2.2:3005/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "SERVER_URL", "\"https://ai-audio-book-api-883622140264.us-central1.run.app/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  implementation(project(":core"))

  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.runtime.compose)

  // Wear Compose (not standard Material3 - watch screens use round/square-aware components)
  implementation(libs.androidx.wear.compose.material3)
  implementation(libs.androidx.wear.compose.foundation)
  implementation(libs.androidx.wear.compose.navigation)

  // Wearable Data Layer (auth-pairing relay with the phone app)
  implementation(libs.play.services.wearable)

  // Media3 ExoPlayer - the watch runs its own playback service, standalone from the phone
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.session)
  implementation(libs.androidx.media3.hls)

  // Offline chapter downloads
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.datastore.preferences)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
}
