package com.colleen.s36349879.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.colleen.s36349879.medtrack.data.medication.Medication
import com.colleen.s36349879.medtrack.data.symptom.Symptom
import com.colleen.s36349879.medtrack.data.tip.MedCoachTip
import com.colleen.s36349879.medtrack.data.patient.Patient
import com.colleen.s36349879.medtrack.data.patient.PatientDao
import com.colleen.s36349879.medtrack.data.medication.MedicationDao
import com.colleen.s36349879.medtrack.data.symptom.SymptomDao
import com.colleen.s36349879.medtrack.data.tip.TipDao

/**
 * The Room database for MedTrack.
 */
@Database(
    entities = [
        Patient::class,
        Medication::class,
        Symptom::class,
        MedCoachTip::class],
    version = 1,
    exportSchema = false
)

abstract class MedTrackDatabase: RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun symptomDao(): SymptomDao
    abstract fun tipDao(): TipDao

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