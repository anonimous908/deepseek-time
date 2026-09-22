# ============================================================================
# app/proguard-rules.pro
# ----------------------------------------------------------------------------
# Reglas de ProGuard/R8. En el MVP la minificación está DESACTIVADA
# (isMinifyEnabled = false), por lo que estas reglas solo aplican a builds
# release futuros. Se dejan las mínimas necesarias para no romper el widget.
# ============================================================================

# Conservar las clases de componentes Android (Activities, Receivers,
# Providers, Services) instanciadas por el sistema a través del manifest.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.appwidget.AppWidgetProvider

# Los AppWidgetProvider se referencian desde el manifest por nombre de clase:
# sin esta regla, R8 podría renombrarlos y el widget dejaría de encontrarse.
-keep class com.protas.time_deepseek.widget.** { *; }
