package com.colleen.s36349879.medtrack.data.doctorreview

import androidx.room.TypeConverter

/**
 * Room does not persist enum columns natively, so [FlaggedItem] needs explicit
 * converters. Register this class with `@TypeConverters(FlaggedItemConverters::class)`
 * on [com.colleen.s36349879.medtrack.data.MedTrackDatabase].
 */
class FlaggedItemConverters {

    @TypeConverter
    fun fromSourceTab(value: FlaggedSourceTab): String = value.name

    @TypeConverter
    fun toSourceTab(value: String): FlaggedSourceTab = FlaggedSourceTab.valueOf(value)

    @TypeConverter
    fun fromReviewStatus(value: ReviewStatus): String = value.name

    @TypeConverter
    fun toReviewStatus(value: String): ReviewStatus = ReviewStatus.valueOf(value)
}
