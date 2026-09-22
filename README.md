# DeepSeek Time

App Android (Kotlin + Jetpack Compose) que indica en tiempo real si la API de DeepSeek está en
tarifa **PEAK** u **OFF-PEAK**, con la cuenta regresiva al próximo cambio y los precios vigentes.

El entregable principal es un **widget de pantalla de inicio**, para no tener que abrir nada.

## Estado

**v0.1.1 disponible**. Versión estable con dependencias de sistema actualizadas (`core-ktx:1.19.0`, `lifecycle:2.11.0`), widget de pantalla de inicio y visualizador interactivo. Podés descargar el APK directamente desde [Releases](https://github.com/anonimous908/deepseek-time/releases) o gestionarlo con Obtainium.

## Instalación y actualizaciones

### Recomendado: Obtainium

[Obtainium](https://github.com/ImranR98/Obtainium) instala y actualiza la app **directamente desde los
Releases de este repositorio**, sin ninguna tienda de por medio. No es una tienda: solo vigila la
página de releases y te avisa cuando hay versión nueva.

1. Instala Obtainium desde su [página de releases](https://github.com/ImranR98/Obtainium/releases)
   — no está en Play Store, se instala como APK.
2. Concédele el permiso de **instalar apps desconocidas** cuando lo pida.
3. En Obtainium, pulsa **+** y pega la URL de este repositorio.
4. Listo. A partir de ahí te avisa solo.

Dos ajustes que conviene hacer:

- Deja el intervalo de comprobación en segundo plano en **6 horas** (es el valor por defecto).
- **Excluye Obtainium de la optimización de batería.** Si no, Android lo duerme y los avisos no llegan.

### Alternativa: APK manual

Descarga el APK más reciente desde la página de Releases y instálalo. Para actualizar, repite el
proceso a mano. Funciona, pero hay que acordarse de mirar.

> **Nota**: Android exige que las actualizaciones estén firmadas con la misma clave que la instalación
> original. Si alguna vez instalaste la app desde otra fuente, tendrás que desinstalarla antes.

## Cómo funciona

DeepSeek cobra distinto según la hora, y el off-peak es **exactamente la mitad** del precio peak:

| Tarifa | Horario UTC | Días |
|---|---|---|
| **PEAK** | 01:00–04:00 y 06:00–10:00 | Lunes a viernes |
| **OFF-PEAK** | el resto | Sábado y domingo, 24 h |

Los precios **no están escritos en el código**: viven en
[`app/src/main/assets/pricing_catalog.json`](app/src/main/assets/pricing_catalog.json). El catálogo
solo guarda los precios peak; el off-peak se deriva dividiendo entre dos, para que no puedan
contradecirse entre sí.

> **Limitación conocida**: hoy el catálogo viaja **dentro del APK**, así que los precios solo cambian
> cuando se publica una versión nueva. Un catálogo remoto está previsto, pero todavía no está.

## Desarrollo

El proyecto se construye **sin conexión** (`./gradlew --offline`): todas las dependencias están
fijadas a versiones presentes en la caché local.
