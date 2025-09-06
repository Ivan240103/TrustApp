package ivandesimone.trustapp.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Client to make Retrofit remote calls.
 */
class RetrofitClientInstance {

	companion object {
		// mock source of data, generates random numbers
		private const val BASE_URL = "https://www.randomnumberapi.com/"
		private lateinit var retrofit: Retrofit

		/**
		 * Get the retrofit singleton.
		 * @return retrofit instance
		 */
		fun getRetrofitInstance(): Retrofit {
			if (!this::retrofit.isInitialized) {
				retrofit = Retrofit.Builder()
					.baseUrl(BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.build()
			}
			return retrofit
		}
	}
}