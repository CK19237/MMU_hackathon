package com.colleen.s36349879.medtrack.data.patient

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a patient record in the local Room database.
 *
 * This data class is annotated with [@Entity] to define it as a Room database table.
 * Unlike auto-generated numeric IDs, the patient ID is a manually assigned string
 * (e.g. "P001") to allow pre-seeded patients from CSV to retain their original IDs.
 */
@Entity(tableName = "patient_table")
data class Patient(

    /**
     * The unique identifier for this patient (e.g. "P001").
     * Manually assigned rather than auto-generated to support CSV seeding
     * and account claiming by pre-existing patients.
     */
    @PrimaryKey
    val patientId: String,

    val phoneNumber: String,
    val patientName: String,
    val password: String,

    /** The patient's age in years. Null for accounts created before this field existed, or left blank at signup. */
    val age: Int? = null,

    /** BCP-47-ish UI language code (e.g. "en", "zh", "ms"). Defaults to English for accounts created before this field existed. */
    val preferredLanguage: String = "en",

    /**
     * Manually-chosen font size, stored as [FontSizeOption.name] (e.g. "LARGE").
     * Null means "no explicit choice yet" — the effective size then falls back to
     * age-based auto-sizing (see [effectiveFontSizeOption]).
     */
    val fontSizePreference: String? = null,

    /** Comma-separated medicine names the patient has recorded as allergies. */
    val medicineAllergies: String = ""
)
