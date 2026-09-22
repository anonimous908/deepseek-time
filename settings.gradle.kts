// ============================================================================
// settings.gradle.kts — configuración del build raíz
// ----------------------------------------------------------------------------
// Define de dónde se resuelven los plugins y las dependencias, y qué módulos
// forman parte del build. El proyecto tiene un único módulo (`:app`).
//
// Restricción dura del proyecto: TODO debe compilar con `--offline`, usando
// únicamente artefactos ya presentes en la caché local de Gradle. Por eso los
// repositorios declarados son los "oficiales" (google/mavenCentral), que son
// los que poblaron la caché.
// ============================================================================

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Se prohíbe que un módulo declare sus propios repositorios: así el origen
    // de cada dependencia queda centralizado aquí (y el build offline es
    // reproducible).
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "deepseek-time"

// Único módulo del proyecto: la app Android (y su widget).
include(":app")
