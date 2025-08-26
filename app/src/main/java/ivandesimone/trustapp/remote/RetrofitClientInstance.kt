package ivandesimone.trustapp.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClientInstance {
	companion object {
		private lateinit var retrofit: Retrofit
		private const val BASE_URL = "https://www.randomnumberapi.com/"

		fun getRetrofitInstance(): Retrofit {
			if (!this::retrofit.isInitialized) {
				retrofit =
					Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
						.build()
			}
			return retrofit
		}
	}
}