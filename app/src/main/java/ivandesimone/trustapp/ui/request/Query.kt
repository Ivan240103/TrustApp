package ivandesimone.trustapp.ui.request

/**
 * Query to perform against Gate's ledger.
 * @param topic typer of sensor
 * @param geo area information
 */
data class Query(
	val topic: String,
	val geo: Geo
)

/**
 * Area information.
 * @param type often "Feature"
 * @param geometry location info
 * @param properties additional info
 */
data class Geo(
	val type: String,
	val geometry: Geometry,
	val properties: Properties
)

/**
 * Location info.
 * @param type often "Point"
 * @param coordinates [lat, long] coordinates
 */
data class Geometry(
	val type: String,
	val coordinates: List<Double>
)

/**
 * Additional info.
 * @param radius area's radius
 */
data class Properties(
	val radius: Int
)