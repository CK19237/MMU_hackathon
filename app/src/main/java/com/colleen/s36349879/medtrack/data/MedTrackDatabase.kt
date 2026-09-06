package com.colleen.s36349879.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.colleen.s36349879.medtrack.data.medication.Medication
import com.colleen.s36349879.medtrack.data.symptom.Symptom
import com.colleen.s36349879.medtrack.data.tip.MedCoachTip
import com.colleen.s36349879.medtrack.data.patient.Patient
import com.colleen.s36349879.medtrack.data.patient.PatientDao
import com.colleen.s36349879.medtrack.data.medication.MedicationDao
import com.colleen.s36349879.medtrack.data.symptom.SymptomDao
import com.colleen.s36349879.medtrack.data.tip.TipDao
import com.colleen.s36349879.medtrack.data.factcheck.MythCache
import com.colleen.s36349879.medtrack.data.factcheck.MythCacheDao
import com.colleen.s36349879.medtrack.data.factcheck.GovEvidence
import com.colleen.s36349879.medtrack.data.factcheck.GovEvidenceDao
import com.colleen.s36349879.medtrack.data.factcheck.VerifiedClaimCache
import com.colleen.s36349879.medtrack.data.factcheck.VerifiedClaimCacheDao
import com.colleen.s36349879.medtrack.data.factcheck.FactCheckHistory
import com.colleen.s36349879.medtrack.data.factcheck.FactCheckHistoryDao
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItem
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItemDao
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItemConverters

/**
 * The Room database for MedTrack.
 *
 * Version bumped 1 -> 2 to add [MythCache] (Fact-Check local myth cache) and
 * [FlaggedItem] (async Doctor Review queue), then 2 -> 3 to add [GovEvidence]
 * (curated Malaysian government evidence index) and [VerifiedClaimCache]
 * (dynamic cache of previously live-verified claims — the "database-first"
 * fact-checking pipeline), then 3 -> 4 to add patient-preference columns
 * (age/preferredLanguage/fontSizePreference) and [FactCheckHistory] (the
 * per-patient fact-check History screen). [MIGRATION_3_4] preserves existing
 * local data on that last upgrade; `fallbackToDestructiveMigration` remains as
 * a safety net for any earlier, un-migrated version jump.
 */
@Database(
    entities = [
        Patient::class,
        Medication::class,
        Symptom::class,
        MedCoachTip::class,
        MythCache::class,
        FlaggedItem::class,
        GovEvidence::class,
        VerifiedClaimCache::class,
        FactCheckHistory::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(FlaggedItemConverters::class)
abstract class MedTrackDatabase: RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun symptomDao(): SymptomDao
    abstract fun tipDao(): TipDao
    abstract fun mythCacheDao(): MythCacheDao
    abstract fun flaggedItemDao(): FlaggedItemDao
    abstract fun govEvidenceDao(): GovEvidenceDao
    abstract fun verifiedClaimCacheDao(): VerifiedClaimCacheDao
    abstract fun factCheckHistoryDao(): FactCheckHistoryDao

    companion object{
        @Volatile
        private var Instance: MedTrackDatabase? = null

        /**
         * Adds the age/preferredLanguage/fontSizePreference columns to patient_table
         * (existing rows default to no age, English, and age-based auto font-sizing)
         * and creates fact_check_history_table. Written as a real Migration — rather
         * than relying on `fallbackToDestructiveMigration` like earlier version bumps —
         * so existing patients/medications/symptoms survive this update.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patient_table ADD COLUMN age INTEGER")
                db.execSQL("ALTER TABLE patient_table ADD COLUMN preferredLanguage TEXT NOT NULL DEFAULT 'en'")
                db.execSQL("ALTER TABLE patient_table ADD COLUMN fontSizePreference TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fact_check_history_table (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patientId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        originalMessage TEXT NOT NULL,
                        verdict TEXT NOT NULL,
                        explanation TEXT NOT NULL,
                        sourceNames TEXT NOT NULL,
                        sourceUrls TEXT NOT NULL,
                        sourceTypes TEXT NOT NULL,
                        languageCode TEXT NOT NULL,
                        officialEvidenceFound INTEGER NOT NULL,
                        checkedAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /** Adds patient medicine-allergy context without destroying existing local data. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patient_table ADD COLUMN medicineAllergies TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): MedTrackDatabase{
            return Instance ?: synchronized(this){
                Room.databaseBuilder(
                    context.applicationContext,
                    MedTrackDatabase::class.java,
                    "medtrack_db"
                ).addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration(true)
                    .build().also { Instance = it}
            }
        }
    }
}