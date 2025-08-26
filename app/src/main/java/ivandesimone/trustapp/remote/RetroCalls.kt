package ivandesimone.trustapp.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetroCalls {
	@GET("/api/v1.0/random?min=100&max=10000")
	fun getValues(@Query("count") count: Byte): Call<List<Int>>
}