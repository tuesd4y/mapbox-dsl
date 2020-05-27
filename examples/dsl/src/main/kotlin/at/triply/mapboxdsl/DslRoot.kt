package at.triply.mapboxdsl

import org.w3c.fetch.Response
import kotlin.browser.window
import kotlin.js.Console
import kotlin.js.Json
import kotlin.js.JSON
import kotlin.js.json


@DslMarker
annotation class MapboxDsl

interface DslItem {
    fun toJson(): Any
}

@MapboxDsl
class Map(
    var container: String = "",
    var accessToken: String = "",
    var style: String = "",
    var bearing: Double = 0.0,
    var pitch: Double = 0.0,
    var zoom: Double = 0.0,
    var center: Point? = null,
    var layers: Layers = Layers()
) {
    fun layers(block: Layers.() -> Unit) {
        this.layers = Layers().apply(block)
    }

    fun show() {
        mapboxgl.accessToken = accessToken
        val map = mapboxgl.Map(jsonConfig())
        map.on("load") {
            layers.forEach {
                console.log(JSON.stringify(it.toJson()))
                map.addLayer(it.toJson())
            }
        }
    }

    fun jsonConfig() = json(
        "container" to container,
        "style" to style,
        "bearing" to bearing,
        "pitch" to pitch,
        "zoom" to zoom,
        "center" to center?.toJson()
    )
}

@MapboxDsl
class Layers : ArrayList<Layer>(), DslItem {
    fun fillExtrusionLayer(block: Layer.FillExtrusionLayer.() -> Unit) = add(
        Layer.FillExtrusionLayer()
            .apply(block)
    )

    override fun toJson() = map { it.toJson() }.toTypedArray()
}

fun map(block: Map.() -> Unit) = Map().apply(block)

@MapboxDsl
class Point(var lat: Double = 0.0, var long: Double = 0.0) : DslItem {
    override fun toJson() = json("lat" to lat, "lon" to long)
}

fun point(lat: Double, long: Double) = Point(lat, long)

@MapboxDsl
sealed class Layer(var id: String = "", var source: Source? = null, var paint: Paint, var layerType: LayerType) :
    DslItem {
    class FillExtrusionLayer(
        id: String = "",
        source: Source? = null,
        var _paint: FillExtrusionPaint = FillExtrusionPaint()
    ) : Layer(id, source, _paint, LayerType.FillExtrusionLayer) {
        fun paint(block: FillExtrusionPaint.() -> Unit) {
            this.paint = FillExtrusionPaint().apply(block)
        }
    }

    override fun toJson() = json(
        "id" to id,
        "type" to layerType.prefix,
        "source" to source?.toJson(),
        "paint" to paint.toJson()
    )
}

interface Paint : DslItem

@MapboxDsl
class FillExtrusionPaint() : Paint {
    var color: Expression<String> = l("#ffffff")
    var height: Expression<Number> = l(0.0)
    var opacity: Expression<Number> = l(0.0)
    var opacityTransition: OpacityTransition? = null

    @MapboxDsl
    class OpacityTransition(var duration: Expression<Number> = l(0.0), var delay: Expression<Number> = l(0.0)) : DslItem {
        override fun toJson() = json("duration" to duration.toJson(), "delay" to delay.toJson())
    }

    fun transition(block: OpacityTransition.() -> Unit) {
        this.opacityTransition = OpacityTransition().apply(block)
    }

    override fun toJson() = arrayOf(
        "${LayerType.FillExtrusionLayer.prefix}-color" to color.toJson(),
        "${LayerType.FillExtrusionLayer.prefix}-height" to height.toJson(),
        "${LayerType.FillExtrusionLayer.prefix}-opacity" to opacity.toJson(),
        "${LayerType.FillExtrusionLayer.prefix}-opacity-transition" to opacityTransition?.toJson()
    )
        .filter { it.second != null }
        .let { json(*it.toTypedArray()) }
}


@MapboxDsl
interface Expression<T : Any> : DslItem

@MapboxDsl
abstract class Literal<T : Any> : Expression<T> {
    abstract fun get(): T
    override fun toJson(): Any {
        return get()
    }
}

class PropertyExpression<T : Any>(val property: String) : Expression<T> {
    override fun toJson() = arrayOf("get", property)
}

fun <T : Any> get(property: String) = PropertyExpression<T>(property)

class MultiplyExpression(val a: Expression<Number>, val b: Expression<Number>) : Expression<Number> {
    override fun toJson() = arrayOf("*", a.toJson(), b.toJson())
}


operator fun Expression<Number>.times(other: Expression<Number>) = MultiplyExpression(this, other)
operator fun Number.times(other: Expression<Number>) = MultiplyExpression(l(this), other)

class RootExpression(val other: Expression<Number>) : Expression<Number> {
    override fun toJson() = arrayOf("sqrt", other.toJson())
}

fun sqrt(expr: Expression<Number>) = RootExpression(expr)

class InterpolateExpression<T : Any>(
    val method: InterpolationMethod,
    val value: Expression<Number>,
    vararg val stops: Pair<Number, T>
) : Expression<T> {
    override fun toJson(): Array<Any> {
        val list = mutableListOf("interpolate", arrayOf(method.value), value.toJson())
        stops.forEach { (stop, stopValue) ->
            list.add(stop)
            list.add(stopValue)
        }
        return list.toTypedArray()
    }
}

/**
 * Produces continuous, smooth results by linearly interpolating between pairs of input and output values ("stops").
 * The input may be any numeric expression (e.g., ["get", "population"]).
 * Stop inputs must be numeric literals in strictly ascending order.
 * The output type must be number, array<number>, or color.
 *
 * More information on [The mapbox style docs](https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#interpolate)
 */
fun <T : Any> interpolateLinear(value: Expression<Number>, vararg stops: Pair<Number, T>) = InterpolateExpression(
    InterpolationMethod.LINEAR, value, *stops
)

enum class InterpolationMethod(val value: String) {
    LINEAR("linear")
}

enum class LayerType(val prefix: String) {
    FillExtrusionLayer("fill-extrusion")
}

sealed class Source(var type: SourceType) : DslItem {
    class VectorSource : Source(SourceType.VECTOR)
    class GeoJsonSource : Source(SourceType.GEOJSON)

    var dataUrl: String? = null
    var dataObject: Json? = null

    override fun toJson() = json(
        "type" to type.value,
        "data" to (dataObject ?: dataUrl ?: "")
    )

}

enum class SourceType(val value: String) {
    VECTOR("vector"),
    GEOJSON("geojson"),
}


fun Layer.vectorSource(block: Source.VectorSource.() -> Unit) {
    source = Source.VectorSource().apply(block)
}

fun Layer.geoJsonSource(block: Source.GeoJsonSource.() -> Unit) {
    source = Source.GeoJsonSource().apply(block)
}

fun <T : Any> l(value: T): Literal<T> {
    return object : Literal<T>() {
        override fun get(): T = value
    }
}
