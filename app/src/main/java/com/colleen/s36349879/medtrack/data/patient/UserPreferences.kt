package com.colleen.s36349879.medtrack.data.patient

/**
 * UI language options for the app's own chrome (navigation, buttons, titles, etc).
 * Separate from [com.colleen.s36349879.medtrack.data.factcheck.FactCheckLanguage], which
 * only controls the language of the Fact-Check AI's response text.
 */
enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    CHINESE("zh", "中文"),
    BAHASA_MALAYSIA("ms", "Bahasa Malaysia");

    companion object {
        val DEFAULT = ENGLISH
        fun fromCode(code: String?): AppLanguage = entries.find { it.code == code } ?: DEFAULT
    }
}

/** Manually-selectable font size options for Settings. */
enum class FontSizeOption(val displayName: String, val scale: Float) {
    DEFAULT("Default", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra Large", 1.3f);

    companion object {
        fun fromStorageKey(key: String?): FontSizeOption? = entries.find { it.name == key }
    }
}

/** The age at or above which the app defaults to the larger accessibility font size. */
const val ACCESSIBILITY_AGE_THRESHOLD = 60

/**
 * The font size that should actually be applied for [this] patient: the user's manual
 * [Patient.fontSizePreference] if they ever set one, otherwise [FontSizeOption.LARGE] if
 * they are [ACCESSIBILITY_AGE_THRESHOLD] or older, otherwise [FontSizeOption.DEFAULT].
 */
fun Patient?.effectiveFontSizeOption(): FontSizeOption {
    if (this == null) return FontSizeOption.DEFAULT
    FontSizeOption.fromStorageKey(fontSizePreference)?.let { return it }
    return if ((age ?: 0) >= ACCESSIBILITY_AGE_THRESHOLD) FontSizeOption.LARGE else FontSizeOption.DEFAULT
}

fun Patient?.appLanguage(): AppLanguage = AppLanguage.fromCode(this?.preferredLanguage)
