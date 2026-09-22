package com.protas.time_deepseek.data

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Valida el esquema y contrato estructural entre `pricing_catalog.json` y `parseCatalog`.
 *
 * Comprueba estáticamente que todas las claves requeridas por el parser existan en el recurso JSON,
 * evitando discrepancias silenciosas de deserialización en tiempo de ejecución sin requerir el runtime de Android.
 */
class CatalogContractTest {

    private val catalogFile = File("src/main/assets/pricing_catalog.json")
    private val catalogText = catalogFile.readText()

    /** Nombres de clave que `parseCatalog` pide con los `get...`. Si uno cambia, esto salta. */
    private val keysReadByTheParser = listOf(
        "schemaVersion",
        "catalogVersion",
        "updatedAt",
        "staleAfterDays",
        "currency",
        "unit",
        "models",
        "retiredModelIds",
        "id",
        "displayName",
        "status",
        "peak",
        "cacheHit",
        "cacheMiss",
        "output"
    )

    @Test
    fun theCatalogFileIsWhereTheRepositoryLooksForIt() {
        assertTrue(
            "AssetsPricingRepository abre 'pricing_catalog.json' desde assets/",
            catalogFile.exists()
        )
    }

    @Test
    fun everyKeyTheParserReadsExistsInTheCatalog() {
        val missing = keysReadByTheParser.filterNot { catalogText.contains("\"$it\"") }
        assertTrue("Claves que el parser pide y el JSON no tiene: $missing", missing.isEmpty())
    }

    /**
     * Valida que el literal de estado activo esperado por el dominio persista en el catálogo JSON.
     */
    @Test
    fun theStatusValueTheParserMatchesIsStillThere() {
        assertTrue(
            "modelStatusOf compara contra \"active\"",
            catalogText.contains("\"active\"")
        )
    }
}
