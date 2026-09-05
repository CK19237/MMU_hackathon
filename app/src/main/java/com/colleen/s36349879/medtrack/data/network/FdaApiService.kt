package com.colleen.s36349879.medtrack.data.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {

    /**
     * Fetches drug label information from the openFDA API matching the given search query.
     *
     * @param search The search query string (e.g. "openfda.brand_name:\"Paracetamol\"").
     * @param limit The maximum number of results to return. Defaults to 1 to retrieve
     * only the closest match.
     * @return A [Response] wrapping a [ResponseModel] containing the drug label results.
     */
    @GET("drug/label.json") // Appended to BASE_URL to form the full endpoint: https://api.fda.gov/drug/label.json
    suspend fun getDrugInfo(
        @Query("search") search: String, // Mapped to the "search" query parameter in the URL
        @Query("limit") limit: Int = 1 // Mapped to the "limit" query parameter; defaults to 1 result
    ): Response<ResponseModel>


    companion object {

        /**
         * The base URL for all openFDA API requests.
         * All endpoint paths defined in this interface are appended to this URL.
         */
        private const val BASE_URL = "https://api.fda.gov/"


        /**
         * Builds and returns a Retrofit-generated implementation of [APIService].
         *
         * Uses [GsonConverterFactory] to automatically deserialize JSON responses
         * into Kotlin data classes.
         *
         * @return A ready-to-use [APIService] instance.
         */
        fun create(): APIService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(APIService::class.java)
        }
    }
}