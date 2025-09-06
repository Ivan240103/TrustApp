package ivandesimone.trustapp.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Operations against the remote source
 */
interface RetroCalls {

	/**
	 * Request one or more random numbers between 100 and 10000.
	 * @param count number of numbers desired, between 1 and 100
	 * @return request's result
	 */
	@GET("/api/v1.0/random?min=100&max=10000")
	fun getValues(@Query("count") count: Byte): Call<List<Int>>

}