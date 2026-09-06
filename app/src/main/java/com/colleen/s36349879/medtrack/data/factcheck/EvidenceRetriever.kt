package com.colleen.s36349879.medtrack.data.factcheck

/** A single evidence entry matched against the user's claim, with its match strength. */
data class MatchedEvidence(
    val evidence: GovEvidence,
    val score: Int
)

/**
 * The outcome of an evidence-retrieval pass: the matched evidence (best
 * match first, following the claim-type's source hierarchy), or an empty
 * list if nothing in the curated table was relevant.
 */
data class EvidenceBundle(
    val claimType: ClaimType,
    val matches: List<MatchedEvidence>
) {
    val isEmpty: Boolean get() = matches.isEmpty()
}

/**
 * Retrieves relevant evidence from the curated [GovEvidence] table for a
 * claim, using the same word-overlap approach already used for the myth
 * cache (see [FactCheckRepository.checkLocalCache]) rather than introducing
 * embeddings/vector search, per the "don't over-engineer" requirement.
 *
 * Matching + ranking:
 * 1. Score every evidence row by keyword-fragment overlap with the claim.
 * 2. Keep only rows that clear a minimum overlap (avoids one-common-word
 *    false positives, same rationale as the myth cache).
 * 3. Order surviving rows by the claim type's required source hierarchy
 *    (see [ClaimClassifier.sourceOrderFor]), then by score.
 */
object EvidenceRetriever {

    private const val MIN_OVERLAP = 2

    suspend fun retrieve(claim: String, dao: GovEvidenceDao): EvidenceBundle {
        val claimType = ClaimClassifier.classify(claim)
        val claimWords = tokenize(claim)
        if (claimWords.isEmpty()) return EvidenceBundle(claimType, emptyList())

        val sourceOrder = ClaimClassifier.sourceOrderFor(claimType)

        val scored = dao.getAll().mapNotNull { evidence ->
            val evidenceWords = evidence.normalizedKeywords.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val score = evidenceWords.count { keyword -> claimWords.any { it.contains(keyword) || keyword.contains(it) } }
            if (score >= MIN_OVERLAP) MatchedEvidence(evidence, score) else null
        }

        val ordered = scored.sortedWith(
            compareBy(
                { sourceOrder.indexOf(it.evidence.sourceType).let { idx -> if (idx == -1) sourceOrder.size else idx } },
                { -it.score }
            )
        )

        return EvidenceBundle(claimType, ordered)
    }

    private fun tokenize(text: String): List<String> =
        text.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length > 2 }
}
