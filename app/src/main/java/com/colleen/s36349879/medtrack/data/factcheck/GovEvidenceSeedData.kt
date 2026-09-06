package com.colleen.s36349879.medtrack.data.factcheck

/**
 * Seed data for the curated Malaysian government evidence table.
 *
 * Each entry cites the general official portal for its issuing authority
 * (the same top-level URLs used throughout the project's own source
 * hierarchy) rather than a specific sub-page or PDF, since this project has
 * not independently verified that a specific deep link exists for every
 * claim below. Deep-linking to a confirmed specific document is preferable
 * where available — replace [sourceUrl] with the exact document/product page
 * once it has been checked, per the "no invented links" requirement.
 *
 * This list intentionally starts small and Malaysia-context-focused
 * (medicines commonly asked about locally, dengue, and NCD/nutrition
 * statistics). Grow it the same way [MythSeedData] is grown: add an entry,
 * no infrastructure change required.
 */
object GovEvidenceSeedData {

    private const val NPRA_URL = "https://www.npra.gov.my/"
    private const val NPRA_PRODUCT_SEARCH_URL = "https://quest3plus.bpfk.gov.my/pmo2/index.php"
    private const val KKM_PHARMACY_URL = "https://pharmacy.moh.gov.my/"
    private const val KKM_CPG_URL = "https://www.moh.gov.my/en/publications-and-reports/policies-act-policies-guide-lines/penerbitan-klinikal/senarai-penerbitan-klinikal/panduan-amalan-klinikal-cpg"
    private const val MAHTAS_CPG_URL = "https://mymahtas.moh.gov.my/index.php/docman-list/reports/cpg-list"
    private const val NHMS_URL = "https://iku.gov.my/nhms-2024"

    val entries: List<GovEvidence> = listOf(
        GovEvidence(
            claimType = ClaimType.MEDICINE.name,
            normalizedKeywords = "panadol,paracetamol,acetaminophen,fever,pain,stomach,ache",
            evidenceText = "Paracetamol (the active ingredient in Panadol) is registered and indicated for the relief of mild to moderate pain and for reducing fever. It is not indicated for treating stomach conditions, infections, or as a cure for any disease.",
            evidenceTextMs = "Parasetamol (bahan aktif dalam Panadol) didaftarkan dan ditunjukkan untuk melegakan sakit ringan hingga sederhana dan menurunkan demam. Ia tidak ditunjukkan untuk merawat masalah perut, jangkitan, atau sebagai penawar sebarang penyakit.",
            sourceName = "National Pharmaceutical Regulatory Agency (NPRA)",
            sourceType = "NPRA",
            sourceUrl = NPRA_PRODUCT_SEARCH_URL
        ),
        GovEvidence(
            claimType = ClaimType.MEDICINE.name,
            normalizedKeywords = "antibiotic,antibiotics,virus,viral,flu,cold,infection",
            evidenceText = "Antibiotics registered in Malaysia are indicated for bacterial infections. They have no effect on viruses, so they are not an appropriate treatment for the common cold, flu, or other viral illnesses, and inappropriate use contributes to antibiotic resistance.",
            evidenceTextMs = "Antibiotik yang didaftarkan di Malaysia ditunjukkan untuk jangkitan bakteria. Ia tidak memberi kesan terhadap virus, jadi ia bukan rawatan yang sesuai untuk selesema biasa, influenza, atau penyakit virus lain.",
            sourceName = "Ministry of Health Malaysia — Pharmaceutical Services Programme",
            sourceType = "KKM_PHARMA",
            sourceUrl = KKM_PHARMACY_URL
        ),
        GovEvidence(
            claimType = ClaimType.MEDICINE.name,
            normalizedKeywords = "aspirin,ibuprofen,dengue,fever,bleeding,nsaid",
            evidenceText = "Patients with suspected dengue are advised to avoid aspirin and NSAIDs such as ibuprofen, as these can increase bleeding risk; paracetamol is the recommended option for fever/pain relief in suspected dengue, alongside medical monitoring.",
            evidenceTextMs = "Pesakit yang disyaki menghidap denggi dinasihatkan mengelakkan aspirin dan NSAID seperti ibuprofen kerana ia boleh meningkatkan risiko pendarahan; parasetamol disyorkan untuk demam/kesakitan, di samping pemantauan perubatan.",
            sourceName = "Ministry of Health Malaysia — Clinical Practice Guidelines",
            sourceType = "KKM_CPG",
            sourceUrl = KKM_CPG_URL
        ),
        GovEvidence(
            claimType = ClaimType.DISEASE_TREATMENT.name,
            normalizedKeywords = "papaya,leaves,rice water,dengue,platelets,juice",
            evidenceText = "Malaysian clinical guidance for dengue centres on hydration, monitoring of warning signs, and medical care; home remedies such as papaya leaf juice or rice water are not a substitute for this monitoring, since dengue can deteriorate quickly.",
            evidenceTextMs = "Panduan klinikal Malaysia untuk denggi menumpukan kepada penghidratan, pemantauan tanda amaran, dan rawatan perubatan; ubat rumah seperti jus daun betik atau air beras bukan pengganti pemantauan ini.",
            sourceName = "Ministry of Health Malaysia — Clinical Practice Guidelines",
            sourceType = "KKM_CPG",
            sourceUrl = KKM_CPG_URL
        ),
        GovEvidence(
            claimType = ClaimType.DISEASE_TREATMENT.name,
            normalizedKeywords = "diabetes,herbal,traditional,cure,insulin,stop medication",
            evidenceText = "Malaysian clinical practice guidelines manage diabetes through prescribed medication, diet, and monitoring; no herbal or traditional preparation is recognised as a cure, and stopping prescribed diabetes medication without medical advice is considered unsafe.",
            evidenceTextMs = "Garis panduan amalan klinikal Malaysia menguruskan diabetes melalui ubat yang ditetapkan, diet, dan pemantauan; tiada ubat herba atau tradisional diiktiraf sebagai penawar.",
            sourceName = "Ministry of Health Malaysia — Clinical Practice Guidelines",
            sourceType = "KKM_CPG",
            sourceUrl = MAHTAS_CPG_URL
        ),
        GovEvidence(
            claimType = ClaimType.DISEASE_TREATMENT.name,
            normalizedKeywords = "hypertension,blood pressure,salt,sodium,high blood",
            evidenceText = "Malaysian clinical practice guidelines for hypertension identify high dietary sodium/salt intake as a modifiable risk factor and recommend reduced salt intake alongside prescribed treatment, not as a standalone cure.",
            evidenceTextMs = "Garis panduan amalan klinikal Malaysia untuk hipertensi mengenal pasti pengambilan garam/natrium yang tinggi sebagai faktor risiko boleh diubah suai dan menyarankan pengurangan garam bersama rawatan yang ditetapkan.",
            sourceName = "Ministry of Health Malaysia — Clinical Practice Guidelines",
            sourceType = "KKM_CPG",
            sourceUrl = KKM_CPG_URL
        ),
        GovEvidence(
            claimType = ClaimType.NUTRITION_POPULATION_HEALTH.name,
            normalizedKeywords = "obesity,overweight,malaysia,prevalence,bmi",
            evidenceText = "The National Health and Morbidity Survey (NHMS) tracks obesity and overweight prevalence among Malaysian adults over time as part of national non-communicable disease surveillance.",
            evidenceTextMs = "Kajian Kebangsaan Kesihatan dan Morbiditi (NHMS) menjejaki kelaziman obesiti dan berat badan berlebihan dalam kalangan orang dewasa Malaysia dari semasa ke semasa.",
            sourceName = "Institute for Public Health (IKU) — National Health and Morbidity Survey",
            sourceType = "NHMS",
            sourceUrl = NHMS_URL
        ),
        GovEvidence(
            claimType = ClaimType.NUTRITION_POPULATION_HEALTH.name,
            normalizedKeywords = "sugar,sugary drinks,diabetes,children,soda,tax",
            evidenceText = "National Health and Morbidity Survey data on dietary behaviour is used by the Ministry of Health to inform nutrition policy, including monitoring sugar-sweetened beverage consumption among the Malaysian population.",
            evidenceTextMs = "Data tingkah laku pemakanan NHMS digunakan oleh Kementerian Kesihatan Malaysia untuk memaklumkan dasar pemakanan, termasuk pemantauan pengambilan minuman bergula.",
            sourceName = "Institute for Public Health (IKU) — National Health and Morbidity Survey",
            sourceType = "NHMS",
            sourceUrl = NHMS_URL
        ),
        GovEvidence(
            claimType = ClaimType.MEDICINE.name,
            normalizedKeywords = "supplement,vitamin,unregistered,traditional medicine,quack",
            evidenceText = "Health supplements and traditional medicines sold in Malaysia are expected to carry valid NPRA registration; products without registration have not undergone NPRA's safety and quality assessment.",
            evidenceTextMs = "Suplemen kesihatan dan ubat tradisional yang dijual di Malaysia perlu mempunyai pendaftaran NPRA yang sah; produk tanpa pendaftaran belum melalui penilaian keselamatan dan kualiti NPRA.",
            sourceName = "National Pharmaceutical Regulatory Agency (NPRA)",
            sourceType = "NPRA",
            sourceUrl = NPRA_URL
        )
    )

    /**
     * Inserts [entries] into the gov evidence table only if it's currently
     * empty. Mirrors [MythSeedData.seedIfEmpty] so it's safe to call on every
     * app start.
     */
    suspend fun seedIfEmpty(dao: GovEvidenceDao) {
        if (dao.count() == 0) {
            dao.insertAll(entries)
        }
    }
}
