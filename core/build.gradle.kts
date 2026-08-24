plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rodrigos01.aiaudiobook.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        shaders = false
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Exposed as api: these types appear in the public signatures of shared repositories/models/
    // ViewModels (e.g. Title.created_at: Timestamp, AuthRepository.currentUser: FirebaseUser,
    // MediaPlaybackService(context, serviceClass: Class<out MediaSessionService>)), so both :app
    // and :wear need them resolvable when compiling against :core, not just at runtime.
    api(platform(libs.firebase.bom))
    api(libs.firebase.auth.ktx)
    api(libs.firebase.firestore.ktx)
    api(libs.androidx.media3.session)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)

    // Internal implementation details, not part of :core's public API surface.
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)


    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
