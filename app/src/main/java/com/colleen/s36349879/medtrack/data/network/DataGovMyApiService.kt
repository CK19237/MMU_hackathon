package com.colleen.s36349879.medtrack.data.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Client for data.gov.my's real, documented Data Catalogue API
 * (confirmed at https://developer.data.gov.my/static-api/data-catalogue —
 * this is a genuine public API, unlike NPRA/KKM/NHMS, which only offer web
 * pages/PDFs; see [com.colleen.s36349879.medtrack.data.factcheck.GovEvidence]
 * for how those are handled instead).
 *
 * NOTE ON THE `id` PARAMETER: the catalogue's dataset ids (e.g. "fuelprice")
 * are only discoverable by browsing https://data.gov.my/data-catalogue and
 * opening a dataset's page, which lists its id under "Sample OpenAPI query".
 * No nutrition/NCD-prevalence dataset id has been verified against the live
 * catalogue as part of this change, so none is hardcoded here — wire up a
 * specific `id` once you've confirmed it on the catalogue page, rather than
 * guessing one. Response shape also varies per dataset, so [rawJson] is
 * intentionally returned as a raw string for the caller to parse once a
 * specific dataset (and its schema) is chosen.
 */
interface DataGovMyApiService {

    @GET("data-catalogue")
    suspend fun getDataset(
        @Query("id") id: String,
        @Query("limit") limit: Int = 10
    ): Response<okhttp3.ResponseBody>

    companion object {
        private const val BASE_URL = "https://api.data.gov.my/"

        fun create(): DataGovMyApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DataGovMyApiService::class.java)
        }
    }
}
