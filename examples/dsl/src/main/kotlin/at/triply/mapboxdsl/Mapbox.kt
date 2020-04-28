package at.triply.mapboxdsl

import kotlin.js.Json

external object mapboxgl {
    var accessToken: String
    class Map(config: Json) {
        fun on(action: String, function: () -> Unit)
        fun addLayer(layer: Json)
    }
}