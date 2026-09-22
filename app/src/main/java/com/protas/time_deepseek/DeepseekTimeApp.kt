package com.protas.time_deepseek

import android.app.Application
import android.content.Context
import com.protas.time_deepseek.data.AssetsPricingRepository
import com.protas.time_deepseek.domain.TariffSchedule
import java.time.Clock

/**
 * Aplicación Android: punto de entrada del ciclo de vida del proceso.
 * Inicializa el ServiceLocator manual para abastecer a la UI y al widget.
 */
class DeepseekTimeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}

/**
 * Inyector de dependencias manual (Service Locator).
 *
 * Expone las tres dependencias del sistema compartidas por la UI y el widget:
 * 1. [clock]: Reloj de la app (`Clock.systemUTC()` en producción, sustituible en tests).
 * 2. [pricingRepository]: Repositorio que lee el catálogo empaquetado en assets.
 * 3. [tariffSchedule]: Reglas de horarios de DeepSeek (objeto de dominio puro).
 */
object ServiceLocator {

    var clock: Clock = Clock.systemUTC()

    val tariffSchedule: TariffSchedule = TariffSchedule

    private var _pricingRepository: AssetsPricingRepository? = null

    val pricingRepository: AssetsPricingRepository
        get() = checkNotNull(_pricingRepository) {
            "ServiceLocator no inicializado. Asegúrate de llamar a ServiceLocator.init(context)."
        }

    fun init(context: Context) {
        if (_pricingRepository == null) {
            _pricingRepository = AssetsPricingRepository(context.applicationContext.assets)
        }
    }

    /**
     * Permite sobreescribir o resetear dependencias en tests de integración o instrumentados.
     */
    fun resetForTesting(
        customClock: Clock? = null,
        customRepository: AssetsPricingRepository? = null
    ) {
        clock = customClock ?: Clock.systemUTC()
        _pricingRepository = customRepository
    }
}
