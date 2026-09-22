package com.protas.time_deepseek.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.protas.time_deepseek.MainActivity
import com.protas.time_deepseek.R
import com.protas.time_deepseek.ServiceLocator
import com.protas.time_deepseek.domain.TariffSchedule
import com.protas.time_deepseek.domain.TariffState
import com.protas.time_deepseek.util.PriceFormatter
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/**
 * Proveedor del widget de pantalla de inicio ([AppWidgetProvider]).
 *
 * Arquitectura y ciclo de vida:
 * 1. [onUpdate]: Inicializa dependencias, compone las vistas remotas y programa la alarma exacta para la siguiente transición.
 * 2. [onReceive]: Atiende [ACTION_ALARM] emitida en las fronteras de cambio de franja tarifaria.
 * 3. [onDisabled]: Libera recursos y cancela alarmas pendientes al eliminar la última instancia del widget.
 * 4. Utiliza [android.widget.Chronometer] nativo con cuenta regresiva para delegar la animación en SystemUI sin despertar el proceso de la app.
 * 5. Dibuja el mini-timeline de 24 h UTC en un [Bitmap] optimizado mediante [Canvas] para mantenerse dentro de los límites de memoria de Binder IPC.
 */
class TariffWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ALARM = "com.protas.time_deepseek.widget.ACTION_ALARM"

        // Colores del mini-timeline (alineados con colors.xml)
        private const val COLOR_PEAK = 0xFFB3261E.toInt()
        private const val COLOR_OFF_PEAK = 0xFF2E7D32.toInt()
        private const val COLOR_NEEDLE = 0xFFFFFFFF.toInt()

        private const val TIMELINE_WIDTH = 480
        private const val TIMELINE_HEIGHT = 28
        private const val TIMELINE_CORNER_RADIUS = 8f
        private const val SECONDS_IN_DAY = 86_400f
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_ALARM) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TariffWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        ServiceLocator.init(context)

        val now = ServiceLocator.clock.instant()
        val currentWindow = ServiceLocator.tariffSchedule.currentWindow(now)
        val tariffState = currentWindow.state
        val nextTransition = currentWindow.end

        // Programar la alarma para la próxima frontera de tarifa
        scheduleNextAlarm(context, nextTransition)

        // Renderizar y enviar la vista remota a cada instancia del widget
        val remoteViews = buildRemoteViews(context, now, tariffState, nextTransition)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelAlarm(context)
    }

    private fun buildRemoteViews(
        context: Context,
        now: Instant,
        state: TariffState,
        nextTransition: Instant
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_tariff)

        // Estilo visual, etiquetas y badge según la tarifa activa
        val bgDrawable = when (state) {
            TariffState.PEAK -> R.drawable.widget_bg_peak
            TariffState.OFF_PEAK -> R.drawable.widget_bg_offpeak
        }
        views.setInt(R.id.widget_root, "setBackgroundResource", bgDrawable)

        when (state) {
            TariffState.PEAK -> {
                views.setTextViewText(R.id.tv_tariff_label, context.getString(R.string.tariff_peak))
                views.setTextColor(R.id.tv_tariff_label, Color.WHITE)
                views.setTextViewText(R.id.tv_tariff_badge, "⚡ PEAK")
                views.setInt(R.id.tv_tariff_badge, "setBackgroundResource", R.drawable.widget_badge_peak)
                views.setTextViewText(R.id.tv_countdown_label, context.getString(R.string.countdown_to_offpeak))
                views.setTextColor(R.id.ch_countdown, Color.WHITE)
            }
            TariffState.OFF_PEAK -> {
                views.setTextViewText(R.id.tv_tariff_label, context.getString(R.string.tariff_off_peak))
                views.setTextColor(R.id.tv_tariff_label, 0xFF4CAF50.toInt())
                views.setTextViewText(R.id.tv_tariff_badge, "✓ OFF-PEAK")
                views.setInt(R.id.tv_tariff_badge, "setBackgroundResource", R.drawable.widget_badge_offpeak)
                views.setTextViewText(R.id.tv_countdown_label, context.getString(R.string.countdown_to_peak))
                views.setTextColor(R.id.ch_countdown, 0xFF4CAF50.toInt())
            }
        }

        // Cuenta regresiva vinculada al Chronometer nativo del sistema
        val countdownSeconds = Duration.between(now, nextTransition).seconds.coerceAtLeast(0)
        val base = SystemClock.elapsedRealtime() + (countdownSeconds * 1000L)
        val format = context.getString(R.string.countdown_chronometer_format)
        views.setChronometer(R.id.ch_countdown, base, format, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            views.setChronometerCountDown(R.id.ch_countdown, true)
        }

        // Presentación de precios vigentes o banner de modo degradado
        val catalogResult = ServiceLocator.pricingRepository.loadCatalog()
        val catalog = catalogResult.getOrNull()

        if (catalog != null && catalog.activeModels.isNotEmpty()) {
            views.setViewVisibility(R.id.tv_catalog_note, View.GONE)
            val active = catalog.activeModels

            if (active.isNotEmpty()) {
                val m1 = active[0]
                val shortName = m1.displayName.removePrefix("DeepSeek-").replace("-0813", "")
                val formatted = PriceFormatter.format(m1.pricesFor(state), catalog.currency).replace("  •  ", " • ")
                views.setTextViewText(R.id.tv_price_1, "$shortName  $formatted")
                views.setViewVisibility(R.id.tv_price_1, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.tv_price_1, View.GONE)
            }

            if (active.size > 1) {
                val m2 = active[1]
                val shortName = m2.displayName.removePrefix("DeepSeek-").replace("-0813", "")
                val formatted = PriceFormatter.format(m2.pricesFor(state), catalog.currency).replace("  •  ", " • ")
                views.setTextViewText(R.id.tv_price_2, "$shortName  $formatted")
                views.setViewVisibility(R.id.tv_price_2, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.tv_price_2, View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.tv_price_1, View.GONE)
            views.setViewVisibility(R.id.tv_price_2, View.GONE)
            views.setTextViewText(R.id.tv_catalog_note, context.getString(R.string.degraded_title))
            views.setViewVisibility(R.id.tv_catalog_note, View.VISIBLE)
        }

        // Mini-timeline de 24 horas renderizado en Canvas
        val timelineBitmap = drawTimelineBitmap(now)
        views.setImageViewBitmap(R.id.iv_timeline, timelineBitmap)

        // Intención de lanzamiento al pulsar sobre el widget
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        return views
    }

    private fun drawTimelineBitmap(now: Instant): Bitmap {
        val bitmap = Bitmap.createBitmap(TIMELINE_WIDTH, TIMELINE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val clipPath = Path().apply {
            addRoundRect(
                RectF(0f, 0f, TIMELINE_WIDTH.toFloat(), TIMELINE_HEIGHT.toFloat()),
                TIMELINE_CORNER_RADIUS,
                TIMELINE_CORNER_RADIUS,
                Path.Direction.CW
            )
        }
        canvas.clipPath(clipPath)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Franjas del día UTC (00:00 a 24:00)
        val today = now.atZone(ZoneOffset.UTC).toLocalDate()
        val startOfDay = today.atStartOfDay(ZoneOffset.UTC).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()

        val windows = TariffSchedule.timeline(today)
        for (win in windows) {
            val winStart = if (win.start.isBefore(startOfDay)) startOfDay else win.start
            val winEnd = if (win.end.isAfter(endOfDay)) endOfDay else win.end

            if (winStart.isBefore(winEnd)) {
                val startSec = Duration.between(startOfDay, winStart).seconds.coerceAtLeast(0)
                val endSec = Duration.between(startOfDay, winEnd).seconds.coerceAtMost(86400)

                val left = (startSec / SECONDS_IN_DAY) * TIMELINE_WIDTH
                val right = (endSec / SECONDS_IN_DAY) * TIMELINE_WIDTH

                paint.color = when (win.state) {
                    TariffState.PEAK -> COLOR_PEAK
                    TariffState.OFF_PEAK -> COLOR_OFF_PEAK
                }
                canvas.drawRect(left, 0f, right, TIMELINE_HEIGHT.toFloat(), paint)
            }
        }

        // Aguja vertical de posición actual
        val nowSec = Duration.between(startOfDay, now).seconds.coerceIn(0, 86400)
        val markerX = (nowSec / SECONDS_IN_DAY) * TIMELINE_WIDTH

        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_NEEDLE
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(markerX, 0f, markerX, TIMELINE_HEIGHT.toFloat(), markerPaint)

        return bitmap
    }

    private fun scheduleNextAlarm(context: Context, triggerAt: Instant) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = getAlarmPendingIntent(context)
        val triggerMillis = triggerAt.toEpochMilli()

        if (triggerMillis <= System.currentTimeMillis()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
        }
    }

    private fun cancelAlarm(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(getAlarmPendingIntent(context))
    }

    private fun getAlarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TariffWidgetProvider::class.java).apply {
            action = ACTION_ALARM
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
