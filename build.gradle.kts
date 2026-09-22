// ============================================================================
// build.gradle.kts (raíz) — declaración de los plugins del build
// ----------------------------------------------------------------------------
// VÍA DE RESOLUCIÓN DE PLUGINS ELEGIDA: (3) `buildscript { classpath(...) }`.
//
// POR QUÉ (bloqueante documentado en la arquitectura §12.2):
//   En la caché offline de esta máquina los *markers* de plugin
//   (`<id>.gradle.plugin`) existen en versiones DESALINEADAS respecto a los
//   *jars* que queremos usar:
//
//     marker cacheado                          jar cacheado que queremos
//     --------------------------------------   ---------------------------
//     com.android.application 9.1.0            -> AGP 9.1.0
//     org.jetbrains.kotlin.android 2.4.0       -> Kotlin 2.4.20
//     org.jetbrains.kotlin.plugin.compose 2.4.20
//
//   El bloque idiomático `plugins { id("...") version "..." }` resuelve el
//   plugin a través de su *marker*; al no existir el marker de `kotlin.android`
//   en 2.4.20 (solo está el 2.4.0), el build `--offline` fallaría — y usar
//   2.4.0 rompería la regla "Kotlin y su Compose Compiler deben coincidir".
//
//   Declarando los plugins por `classpath(...)` solo se necesitan los **jars**
//   (todos cacheados) y luego se aplican en `:app` por `id` SIN versión.
//
// ----------------------------------------------------------------------------
// VERSIONES (fijadas a la caché real; corregidas durante la implementación):
//   Android Gradle Plugin ................ 9.1.0   ← NO 8.11.1 (ver abajo)
//   Kotlin ................................ 2.2.10
//   Compose Compiler Gradle Plugin ........ 2.2.10  (coincide EXACTO con Kotlin)
//   Gradle ................................ 9.3.1   (AGP 9.x exige Gradle 9.x)
//
// ⚠️ CORRECCIÓN respecto al plan inicial (AGP 8.11.1 / compileSdk 36):
//   La caché de Compose de esta máquina solo contiene el BOM 2026.09.00, que
//   resuelve Compose 1.12.1. Esa versión de Compose declara en su metadata
//   `minCompileSdk = 37` y `minAndroidGradlePluginVersion = 9.1.0`. Con
//   AGP 8.11.1 y compileSdk 36 el build falla en `checkDebugAarMetadata` con 18
//   errores (verificado). La plataforma `android-37.0` SÍ está instalada, así
//   que se usa AGP 9.1.0 + compileSdk 37.
//
// ⚠️ AGP 9 trae dos novedades que se DESACTIVAN en gradle.properties para no
//   reescribir todo el build con el DSL nuevo:
//     android.builtInKotlin=false  → se sigue aplicando `kotlin.android` a mano.
//     android.newDsl=false         → se mantiene el DSL `android { }` clásico.
//   Con Kotlin "built-in" activo, AGP 9 **prohíbe** aplicar
//   `org.jetbrains.kotlin.android` (error verificado), y su DSL nuevo cambia la
//   forma del bloque `android { }`.
// ============================================================================

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // Android Gradle Plugin.
        classpath("com.android.tools.build:gradle:9.1.0")
        // Compilador de Kotlin para Android.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
        // Plugin del compilador de Compose (desde Kotlin 2.0 va aparte y su
        // versión debe coincidir EXACTAMENTE con la de Kotlin).
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.10")
    }
}

// La raíz no declara tareas propias: todo el trabajo lo aporta el módulo :app.
