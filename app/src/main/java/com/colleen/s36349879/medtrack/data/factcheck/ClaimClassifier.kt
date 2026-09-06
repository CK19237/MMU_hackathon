package com.colleen.s36349879.medtrack.data.factcheck

/**
 * Lightweight keyword-based claim classifier.
 *
 * Determines which [ClaimType] a claim most likely falls into, which in turn
 * decides the source-consultation order in [EvidenceRetriever] (medicine
 * claims look at NPRA first, disease/treatment claims look at KKM CPG first,
 * nutrition/population claims look at NHMS first — per the required
 * hierarchy). Deliberately simple (word lists, not ML/embeddings) since the
 * existing project has no NLP infrastructure and the spec asks not to add
 * heavy machinery unless genuinely necessary.
 */
object ClaimClassifier {

    private val medicineWords = setOf(
        "panadol", "paracetamol", "acetaminophen", "ibuprofen", "aspirin", "antibiotic",
        "antibiotics", "amoxicillin", "metformin", "lisinopril", "medicine", "medication",
        "drug", "tablet", "capsule", "supplement", "vitamin", "dose", "dosage", "pill"
    )

    private val diseaseTreatmentWords = setOf(
        "dengue", "diabetes", "hypertension", "cancer", "asthma", "arthritis", "flu",
        "covid", "coronavirus", "infection", "cure", "treat", "treatment", "disease",
        "symptom", "blood pressure", "chickenpox", "measles"
    )

    private val nutritionWords = setOf(
        "obesity", "overweight", "diet", "nutrition", "sugar", "salt", "food", "calories",
        "exercise", "physical activity", "smoking", "vaping", "alcohol", "population",
        "malaysians", "prevalence"
    )

    /** Classifies [claim] into a [ClaimType] using simple word matching. Returns [ClaimType.GENERAL] if nothing matches distinctly. */
    fun classify(claim: String): ClaimType {
        val lower = claim.lowercase()

        val medicineScore = medicineWords.count { lower.contains(it) }
        val diseaseScore = diseaseTreatmentWords.count { lower.contains(it) }
        val nutritionScore = nutritionWords.count { lower.contains(it) }

        return when {
            medicineScore == 0 && diseaseScore == 0 && nutritionScore == 0 -> ClaimType.GENERAL
            medicineScore >= diseaseScore && medicineScore >= nutritionScore -> ClaimType.MEDICINE
            diseaseScore >= nutritionScore -> ClaimType.DISEASE_TREATMENT
            else -> ClaimType.NUTRITION_POPULATION_HEALTH
        }
    }

    /**
     * The source-type consultation order for a given [ClaimType], per the
     * spec's hierarchy (source priority depends on claim type, not a single
     * fixed global order).
     */
    fun sourceOrderFor(claimType: ClaimType): List<String> = when (claimType) {
        ClaimType.MEDICINE -> listOf("NPRA", "KKM_PHARMA", "KKM_CPG", "NHMS")
        ClaimType.DISEASE_TREATMENT -> listOf("KKM_CPG", "KKM_PHARMA", "NPRA", "NHMS")
        ClaimType.NUTRITION_POPULATION_HEALTH -> listOf("NHMS", "DATA_GOV_MY", "KKM_CPG", "KKM_PHARMA", "NPRA")
        ClaimType.GENERAL -> listOf("NPRA", "KKM_PHARMA", "KKM_CPG", "NHMS", "DATA_GOV_MY")
    }
}
