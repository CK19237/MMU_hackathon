package com.colleen.s36349879.medtrack.data.network

import com.google.gson.annotations.SerializedName

/**
 * Represents the top-level response returned by the openFDA drug label API.
 *
 * Gson automatically maps the JSON response body into this data class.
 * The [results] list contains the individual drug label entries matching the search query.
 *
 * @property results A list of [DrugResult] entries returned by the API, or null if none were found.
 */
data class ResponseModel(
    @SerializedName("results") var results: List<DrugResult>?
)

/**
 * Represents a single drug label result returned by the openFDA API.
 *
 * Each field corresponds to a specific section of the FDA drug label.
 * All fields are nullable as not every drug label includes every section,
 * and absent fields will be null rather than causing a parsing error.
 *
 * @property purpose A description of what the drug is used to treat or prevent.
 * @property warnings Safety warnings and conditions under which the drug should not be used.
 * @property dosageAndAdministration Instructions on how and how much of the drug to take.
 * @property indicationsAndUsage The medical conditions or symptoms the drug is indicated for.
 * @property inactiveIngredient A list of inactive ingredients contained in the drug.
 */
data class DrugResult(
    @SerializedName("purpose") var purpose: List<String>?,
    @SerializedName("warnings") var warnings: List<String>?,
    @SerializedName("dosage_and_administration") val dosageAndAdministration: List<String>? = null,
    @SerializedName("indications_and_usage") val indicationsAndUsage: List<String>? = null,
    @SerializedName("inactive_ingredient") val inactiveIngredient: List<String>? = null,
)
