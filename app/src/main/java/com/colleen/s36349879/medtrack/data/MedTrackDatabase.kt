package com.colleen.s36349879.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItem
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItemDao
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItemConverters

/**
 * The Room database for MedTrack.
 *
 * Version bumped 1 -> 2 to add [MythCache] (Fact-Check local myth cache) and
 * [FlaggedItem] (async Doctor Review queue). This project already uses
 * `fallbackToDestructiveMigration`, so no manual Migration object is needed for
 * a hackathon build — existing local data is wiped on upgrade instead of migrated.
 * Swap in a real Migration before this ships beyond the hackathon.
 */
@Database(
    entities = [
        Patient::class,
        Medication::class,
        Symptom::class,
        MedCoachTip::class,
        MythCache::class,
        FlaggedItem::class],
    version = 2,
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

    companion object{
        @Volatile
        private var Instance: MedTrackDatabase? = null

        fun getDatabase(context: Context): MedTrackDatabase{
            return Instance ?: synchronized(this){
                Room.databaseBuilder(
                    context.applicationContext,
                    MedTrackDatabase::class.java,
                    "medtrack_db"
                ).fallbackToDestructiveMigration(true)
                    .build().also { Instance = it}
            }
        }
    }
}