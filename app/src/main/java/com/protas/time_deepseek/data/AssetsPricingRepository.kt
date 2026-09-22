package com.protas.time_deepseek.data

import android.content.res.AssetManager
import com.protas.time_deepseek.domain.ModelPricing
import com.protas.time_deepseek.domain.ModelStatus
import com.protas.time_deepseek.domain.PriceSet
import com.protas.time_deepseek.domain.PricingCatalog
import java.time.Instant
import org.json.JSONObject

/**
 * Versión máxima de esquema de catálogo soportada por este cliente.
 */
private const val SUPPORTED_SCHEMA_VERSION = 1
private const val CATALOG_FILE = "pricing_catalog.json"

/**
 * Repositorio encargado de cargar el catálogo de precios empaquetado en los assets de la aplicación.
 * Implementa el contrato de acceso a datos aislando a los consumidores de los detalles de almacenamiento local.
 */
class AssetsPricingRepository(private val assets: AssetManager) {

    /**
     * Carga y procesa el catálogo de precios desde el archivo empaquetado en assets.
     *
     * Captura excepciones mediante [Result] para soportar degradación elegante: si el catálogo
     * es inaccesible o corrupto, el error se propaga hacia el ViewModel para mostrar el estado
     * [com.protas.time_deepseek.domain.CatalogStatus.UNAVAILABLE] sin interrumpir la ejecución de la app.
     */
    fun loadCatalog(): Result<PricingCatalog> = runCatching {
        val json = assets.open(CATALOG_FILE).bufferedReader().use { it.readText() }
        parseCatalog(json)
    }
}

/**
 * Parsea la cadena JSON del catálogo y construye la entidad de dominio [PricingCatalog].
 *
 * Diseñada como función pura desacoplada del framework de Android para facilitar su verificación
 * mediante pruebas unitarias en la JVM sin emulador.
 *
 * @throws org.json.JSONException si la estructura JSON es inválida o faltan atributos obligatorios.
 * @throws IllegalStateException si la versión de esquema supera el límite soportado.
 */
fun parseCatalog(json: String): PricingCatalog {
    val root = JSONObject(json)

    val schemaVersion = root.getInt("schemaVersion")
    if (schemaVersion > SUPPORTED_SCHEMA_VERSION) {
        // Principio fail-fast: rechaza esquemas no compatibles para evitar inconsistencias en las tarifas mostradas.
        error("schemaVersion $schemaVersion no soportada (máximo $SUPPORTED_SCHEMA_VERSION)")
    }

    val modelsJson = root.getJSONArray("models")
    val models = (0 until modelsJson.length()).map { index ->
        val model = modelsJson.getJSONObject(index)
        val peak = model.getJSONObject("peak")
        ModelPricing(
            id = model.getString("id"),
            displayName = model.getString("displayName"),
            status = modelStatusOf(model.getString("status")),
            peak = PriceSet(
                cacheHit = peak.getDouble("cacheHit"),
                cacheMiss = peak.getDouble("cacheMiss"),
                output = peak.getDouble("output")
            )
        )
    }

    val retiredJson = root.getJSONArray("retiredModelIds")
    val retiredModelIds = (0 until retiredJson.length()).map { retiredJson.getString(it) }

    return PricingCatalog(
        schemaVersion = schemaVersion,
        catalogVersion = root.getString("catalogVersion"),
        updatedAt = Instant.parse(root.getString("updatedAt")),
        staleAfterDays = root.getInt("staleAfterDays"),
        currency = root.getString("currency"),
        unit = root.getString("unit"),
        models = models,
        retiredModelIds = retiredModelIds
    )
}

/**
 * Mapea el estado del modelo aplicando una política restrictiva: únicamente el valor "active"
 * se clasifica como [ModelStatus.ACTIVE]; cualquier otro estado desconocido se trata de forma
 * conservadora como [ModelStatus.RETIRED].
 */
private fun modelStatusOf(raw: String): ModelStatus =
    if (raw == "active") ModelStatus.ACTIVE else ModelStatus.RETIRED
