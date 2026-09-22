import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// ============================================================================
// app/build.gradle.kts — módulo :app
// ----------------------------------------------------------------------------
// App Android nativa (Kotlin + Jetpack Compose) cuyo entregable principal es
// un widget de pantalla de inicio.
//
// Los plugins se aplican por `id` SIN versión: se resuelven desde el
// `buildscript { classpath(...) }` del build raíz (ver build.gradle.kts raíz y
// la justificación de la vía elegida). Esto es lo que hace viable el build
// `--offline`.
// ============================================================================

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    // Namespace del código generado (R, BuildConfig). Debe coincidir con el
    // paquete base del proyecto.
    namespace = "com.protas.time_deepseek"

    // compileSdk 37 (plataforma `android-37.0`, instalada en esta máquina).
    //
    // ⚠️ CORRECCIÓN respecto al plan inicial (compileSdk 36): la única versión
    // de Compose cacheada (BOM 2026.09.00 → Compose 1.12.1) declara en su
    // metadata `minCompileSdk = 37` y `minAndroidGradlePluginVersion = 9.1.0`.
    // Con compileSdk 36 el build falla en `checkDebugAarMetadata`. Se usa 37
    // (simple, NO 37.0 como extensión) porque la plataforma `android-37.0` ya
    // está instalada.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.protas.time_deepseek"
        minSdk = 29          // java.time disponible sin desugaring
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.1v"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    // Bytecode Java 11 (el runtime de esta máquina es Java 21; el bytecode de
    // la app se mantiene en 11 por compatibilidad).
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

// JVM target del compilador de Kotlin, alineado con el bytecode de Java (11).
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // --- Núcleo Android / ciclo de vida ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // --- Jetpack Compose (versiones gobernadas por el BOM) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Renderizador de las @Preview de Compose. Solo en debug: no entra en el APK de release.
    debugImplementation(libs.androidx.compose.ui.tooling)

    // --- Test (JVM puro, sin reloj real) ---
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
