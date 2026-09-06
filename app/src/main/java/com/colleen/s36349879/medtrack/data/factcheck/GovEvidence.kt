package com.colleen.s36349879.medtrack.data.factcheck

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single piece of curated evidence sourced from an official Malaysian
 * government health authority (NPRA, KKM Pharmaceutical Services, KKM CPG,
 * or NHMS/IKU).
 *
 * IMPORTANT: None of NPRA, KKM Pharmaceutical Services, KKM CPG, or NHMS
 * publish a public REST API (confirmed by checking their own sites — they
 * only offer web pages, search forms, and downloadable PDFs). Per the
 * "do not invent APIs" requirement, this table is therefore a curated,
 * hand-authored evidence index (same pattern as [MythCache]/[MythSeedData])
 * rather than a live scraper or a wrapper around a nonexistent API. It is
 * meant to be grown over time by adding more entries — each one still
 * carrying a real, verifiable official URL — not by adding infrastructure.
 *
 * For data.gov.my, which *does* have a real, documented API
 * (`api.data.gov.my/data-catalogue`), see
 * [com.colleen.s36349879.medtrack.data.network.DataGovMyApiService] instead;
 * that path is queried live rather than curated here.
 */
@Entity(tableName = "gov_evidence_table")
data class GovEvidence(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Which claim type this evidence is most relevant to; drives source-hierarchy selection. See [ClaimType]. */
    val claimType: String,

    /** Lowercase keyword fragments (comma-separated) used to match incoming claims, same convention as [MythCache.normalizedKeywords]. */
    val normalizedKeywords: String,

    /** Short plain-language statement of what the official source actually says (not a verdict on any specific claim — just the fact). */
    val evidenceText: String,

    /** Malay-language version of [evidenceText], if available. Falls back to [evidenceText] when null. */
    val evidenceTextMs: String? = null,

    /** Display name of the issuing authority, e.g. "National Pharmaceutical Regulatory Agency (NPRA)". */
    val sourceName: String,

    /** One of "NPRA", "KKM_PHARMA", "KKM_CPG", "NHMS", "DATA_GOV_MY" — used for hierarchy ordering and the UI's source-type label. */
    val sourceType: String,

    /** Direct, clickable link to the official page/document this evidence came from. Never a search engine, Wikipedia, or aggregator link. */
    val sourceUrl: String
)

/** Claim categories used to select which official source is consulted first, per the required hierarchy. */
enum class ClaimType {
    MEDICINE,
    DISEASE_TREATMENT,
    NUTRITION_POPULATION_HEALTH,
    GENERAL
}
