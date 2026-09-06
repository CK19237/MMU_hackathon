package com.colleen.s36349879.medtrack.data.factcheck

/**
 * Seed data for the local myth cache: ~15 commonly forwarded health claims
 * with a pre-verified verdict, explanation, and sources. This lets the
 * Fact-Check tab return an instant result for the most common cases even
 * with no network connection or a slow Gemini response.
 *
 * Keywords in [MythCache.normalizedKeywords] are matched against the user's
 * claim using overlap scoring in [FactCheckRepository.checkLocalCache] — they
 * don't need to be exhaustive, just distinctive enough to avoid false matches.
 */
object MythSeedData {

    val entries: List<MythCache> = listOf(
        MythCache(
            title = "Vaccines cause autism",
            normalizedKeywords = "vaccine,vaccines,autism,mmr,jab",
            verdict = "FALSE",
            explanationEn = "Large-scale studies involving millions of children have found no link between vaccines and autism. The original 1998 study that suggested a link was retracted for fraud and its author lost his medical license.",
            explanationMs = "Kajian besar-besaran melibatkan jutaan kanak-kanak tidak menemui sebarang kaitan antara vaksin dan autisme. Kajian asal 1998 yang mencadangkan kaitan ini telah ditarik balik kerana penipuan.",
            sourceNames = "WHO;CDC",
            sourceUrls = "https://www.who.int/news-room/feature-stories/detail/autism-and-vaccines;https://www.cdc.gov/vaccinesafety/concerns/autism.html"
        ),
        MythCache(
            title = "5G networks spread COVID-19",
            normalizedKeywords = "5g,network,towers,covid,coronavirus,radiation",
            verdict = "FALSE",
            explanationEn = "Viruses cannot travel on radio waves or mobile networks. COVID-19 has spread in many countries without 5G infrastructure, and 5G uses non-ionizing radiation which cannot create or transmit a virus.",
            explanationMs = "Virus tidak boleh merebak melalui gelombang radio atau rangkaian mudah alih. COVID-19 telah merebak di banyak negara yang tiada infrastruktur 5G.",
            sourceNames = "WHO",
            sourceUrls = "https://www.who.int/news-room/feature-stories/detail/how-to-report-misinformation-online"
        ),
        MythCache(
            title = "Drinking bleach or MMS cures disease",
            normalizedKeywords = "bleach,mms,chlorine dioxide,disinfectant,drink,cure",
            verdict = "FALSE",
            explanationEn = "Ingesting bleach or 'Miracle Mineral Solution' does not cure any disease and can cause severe, sometimes fatal, chemical burns and organ damage. Health authorities worldwide have issued warnings against this.",
            explanationMs = "Meminum peluntur atau 'Miracle Mineral Solution' tidak merawat sebarang penyakit dan boleh menyebabkan kerosakan organ yang teruk.",
            sourceNames = "FDA;WHO",
            sourceUrls = "https://www.fda.gov/consumers/consumer-updates/danger-dont-drink-miracle-mineral-solution-or-similar-products;https://www.who.int"
        ),
        MythCache(
            title = "Garlic or ginger tea cures COVID-19 / cancer",
            normalizedKeywords = "garlic,ginger,tea,cure,covid,cancer",
            verdict = "MISLEADING",
            explanationEn = "Garlic and ginger have some general health benefits, but no evidence shows they cure COVID-19 or cancer. Relying on them instead of medical treatment can delay proper care.",
            explanationMs = "Bawang putih dan halia mempunyai sedikit manfaat kesihatan, tetapi tiada bukti ia dapat menyembuhkan COVID-19 atau kanser.",
            sourceNames = "WHO",
            sourceUrls = "https://www.who.int/emergencies/diseases/novel-coronavirus-2019/advice-for-public/myth-busters"
        ),
        MythCache(
            title = "Rice water or papaya leaves cure dengue instantly",
            normalizedKeywords = "papaya,leaves,rice water,dengue,platelets",
            verdict = "MISLEADING",
            explanationEn = "Some small studies suggest papaya leaf extract may modestly support platelet counts, but it is not a proven cure and does not replace medical monitoring, which is essential in dengue as it can become severe quickly.",
            explanationMs = "Sesetengah kajian kecil menunjukkan ekstrak daun betik mungkin membantu, tetapi ia bukan penawar terbukti dan tidak menggantikan pemantauan perubatan.",
            sourceNames = "Ministry of Health Malaysia",
            sourceUrls = "https://www.moh.gov.my"
        ),
        MythCache(
            title = "Cold showers or exposure to cold weather causes the flu",
            normalizedKeywords = "cold shower,cold weather,catch a cold,flu,cold air",
            verdict = "FALSE",
            explanationEn = "Colds and flu are caused by viruses, not cold temperatures. People get sick more in winter mainly because they spend more time indoors in close contact with others, not because of the cold itself.",
            explanationMs = "Selesema dan flu disebabkan oleh virus, bukan suhu sejuk. Orang lebih kerap sakit pada musim sejuk kerana lebih banyak masa dihabiskan di dalam rumah berhampiran orang lain.",
            sourceNames = "CDC",
            sourceUrls = "https://www.cdc.gov/flu/about/myths-facts.htm"
        ),
        MythCache(
            title = "MSG (monosodium glutamate) is dangerous and causes headaches for everyone",
            normalizedKeywords = "msg,monosodium glutamate,headache,ajinomoto",
            verdict = "MISLEADING",
            explanationEn = "Major food safety authorities consider MSG safe for the general population at typical dietary levels. A small subset of people report sensitivity, but well-controlled studies have not consistently reproduced a reliable MSG-headache link.",
            explanationMs = "Pihak berkuasa keselamatan makanan menganggap MSG selamat untuk kebanyakan orang pada tahap pemakanan biasa.",
            sourceNames = "FDA",
            sourceUrls = "https://www.fda.gov/food/food-additives-petitions/questions-and-answers-monosodium-glutamate-msg"
        ),
        MythCache(
            title = "Sugar makes children hyperactive",
            normalizedKeywords = "sugar,hyperactive,children,kids,sweets",
            verdict = "FALSE",
            explanationEn = "Multiple controlled studies, including double-blind trials, have found no consistent link between sugar intake and hyperactivity in children. Perceived effects are often due to the excitement of the setting (parties, events) rather than sugar itself.",
            explanationMs = "Beberapa kajian terkawal tidak menemui kaitan yang konsisten antara pengambilan gula dan hiperaktif dalam kalangan kanak-kanak.",
            sourceNames = "American Academy of Pediatrics",
            sourceUrls = "https://www.healthychildren.org"
        ),
        MythCache(
            title = "Cracking your knuckles causes arthritis",
            normalizedKeywords = "cracking knuckles,crack knuckles,arthritis,joints",
            verdict = "FALSE",
            explanationEn = "Research, including studies comparing habitual knuckle-crackers to non-crackers, has found no increased rate of arthritis from knuckle cracking, though it may in rare cases reduce grip strength.",
            explanationMs = "Kajian tidak menemui peningkatan kadar artritis akibat memetik buku jari.",
            sourceNames = "Harvard Health Publishing",
            sourceUrls = "https://www.health.harvard.edu"
        ),
        MythCache(
            title = "Vaccines overload or weaken a baby's immune system",
            normalizedKeywords = "vaccines,overload,immune system,too many vaccines,baby",
            verdict = "FALSE",
            explanationEn = "A baby's immune system encounters far more antigens from everyday environmental exposure than from the entire recommended vaccine schedule combined. Vaccines are rigorously tested for safety, including when given in combination.",
            explanationMs = "Sistem imun bayi terdedah kepada lebih banyak antigen daripada persekitaran harian berbanding keseluruhan jadual vaksin yang disyorkan.",
            sourceNames = "CDC;WHO",
            sourceUrls = "https://www.cdc.gov/vaccines/parents/tools/immunization-schedules.html;https://www.who.int"
        ),
        MythCache(
            title = "Intentionally getting chickenpox (chickenpox parties) is safer than the vaccine",
            normalizedKeywords = "chickenpox party,chicken pox,varicella,natural infection",
            verdict = "FALSE",
            explanationEn = "Deliberately infecting a child with chickenpox carries real risks of complications such as bacterial skin infections, pneumonia, and encephalitis, which are far rarer with the vaccine. Health authorities advise against chickenpox parties.",
            explanationMs = "Menjangkiti kanak-kanak dengan cacar air secara sengaja membawa risiko komplikasi yang lebih besar berbanding vaksin.",
            sourceNames = "CDC",
            sourceUrls = "https://www.cdc.gov/chickenpox/vaccine-info.html"
        ),
        MythCache(
            title = "Detox teas or juice cleanses remove toxins from your body",
            normalizedKeywords = "detox tea,detox,juice cleanse,toxins,cleanse",
            verdict = "FALSE",
            explanationEn = "The liver and kidneys already filter toxins from the body efficiently. There is no good clinical evidence that detox teas or juice cleanses remove additional toxins, and some detox products have caused electrolyte imbalances or liver injury.",
            explanationMs = "Hati dan buah pinggang sudah menapis toksin daripada badan dengan cekap. Tiada bukti klinikal kukuh bahawa teh detoks membantu.",
            sourceNames = "NHS",
            sourceUrls = "https://www.nhs.uk/live-well/eat-well/food-types/detox-diets/"
        ),
        MythCache(
            title = "Alkaline water or alkaline diets cure or prevent cancer",
            normalizedKeywords = "alkaline water,alkaline diet,ph,cancer,acidic",
            verdict = "FALSE",
            explanationEn = "The human body tightly regulates blood pH regardless of diet, and no credible clinical evidence supports alkaline water or diets as a cancer treatment or preventive measure. Relying on this instead of proven treatment can be harmful.",
            explanationMs = "Badan manusia mengawal pH darah secara ketat tanpa mengira diet, dan tiada bukti klinikal menyokong dakwaan ini.",
            sourceNames = "American Cancer Society",
            sourceUrls = "https://www.cancer.org"
        ),
        MythCache(
            title = "Essential oils can cure cancer or replace chemotherapy",
            normalizedKeywords = "essential oils,cure cancer,replace chemotherapy,aromatherapy",
            verdict = "FALSE",
            explanationEn = "No essential oil has been shown in rigorous clinical trials to cure cancer or replace conventional treatment. Some may help with comfort symptoms like nausea, but delaying evidence-based treatment in favor of oils can allow cancer to progress.",
            explanationMs = "Tiada minyak pati terbukti secara klinikal dapat menyembuhkan kanser atau menggantikan rawatan konvensional.",
            sourceNames = "American Cancer Society;WHO",
            sourceUrls = "https://www.cancer.org;https://www.who.int"
        ),
        MythCache(
            title = "You need to 'starve' a fever and 'feed' a cold (or vice versa)",
            normalizedKeywords = "starve a fever,feed a cold,fever,cold,eating sick",
            verdict = "MISLEADING",
            explanationEn = "There's no solid evidence for withholding food during a fever or forcing food during a cold. Staying hydrated and eating as appetite allows is generally recommended for both, and prolonged fasting while sick can be harmful.",
            explanationMs = "Tiada bukti kukuh untuk menahan makanan semasa demam atau memaksa makan semasa selesema. Kekal terhidrat adalah lebih penting.",
            sourceNames = "NHS",
            sourceUrls = "https://www.nhs.uk"
        )
    )

    /**
     * Inserts [entries] into the myth cache only if the table is currently empty,
     * so this is safe to call on every app start (e.g. alongside the existing
     * patient/medication/symptom seeding in MainActivity).
     */
    suspend fun seedIfEmpty(dao: MythCacheDao) {
        if (dao.count() == 0) {
            dao.insertAll(entries)
        }
    }
}
