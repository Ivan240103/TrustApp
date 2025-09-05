package ivandesimone.trustapp.ui.request

data class Query(
	val topic: String,
	val geo: Geo
)

data class Geo(
	val type: String,
	val geometry: Geometry,
	val properties: Properties
)

data class Geometry(
	val type: String,
	val coordinates: List<Double>
)

data class Properties(
	val radius: Int
)