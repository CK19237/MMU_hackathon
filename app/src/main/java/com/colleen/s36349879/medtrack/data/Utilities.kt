package com.colleen.s36349879.medtrack.data

import android.content.Context
import android.content.SharedPreferences
import java.io.BufferedReader
import java.io.InputStreamReader

/** The name of the [SharedPreferences] file */
const val PREFS_NAME = "app_sp"

/** The key used to store and retrieve the database seeding flag in [SharedPreferences]. */
const val KEY_IS_SEEDED = "is_database_seeded"

/**
 * Checks whether the database has already been seeded with initial data.
 *
 * This flag is stored in [SharedPreferences] so it persists across app launches,
 * ensuring the CSV and JSON seeding process only runs once on first install.
 *
 * @param context The context used to access [SharedPreferences].
 * @return True if the database has already been seeded, false otherwise.
 */
fun isDatabaseSeeded(context: Context): Boolean{
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_IS_SEEDED, false) // Defaults to false if the flag has never been set
}

/**
 * Marks the database as seeded by writing a flag to [SharedPreferences].
 *
 * Called after the CSV and JSON seeding process completes successfully,
 * preventing the seeding from running again on subsequent app launches.
 *
 * @param context The context used to access [SharedPreferences].
 */
fun setDatabaseSeeded(context: Context){
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_IS_SEEDED, true).apply()
}

/**
 * Parses a single line of CSV text into a list of individual field values.
 *
 * Handles fields that contain commas by respecting quoted sections and
 * any comma found inside double quotes is treated as part of the field
 * value rather than a column separator.
 *
 * @param line A single raw CSV line string to parse.
 * @return A list of trimmed field value strings parsed from the line.
 */
fun splitCsvLine(line: String): List<String>{
    val result = mutableListOf<String>()
    var current = StringBuilder() // Accumulates characters for the current field
    var insideQuotes = false // Tracks whether the parser is inside a quoted section
    for (ch in line){
        when{
            ch == '"' -> insideQuotes = !insideQuotes // Toggle quote mode on and off when a quote character is encountered
            ch == ',' && !insideQuotes -> { // A comma outside quotes signals the end of a field
                result.add(current.toString().trim()) // Save the completed field and trim any surrounding whitespace
                current = StringBuilder() // Reset the builder for the next field
            }
            else -> current.append(ch) // Any other character is part of the current field value
        }
    }
    result.add(current.toString().trim()) // Add the final field after the last comma
    return result
}