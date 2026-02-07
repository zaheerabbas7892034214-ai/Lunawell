package com.zaheer.lunawell.utils

object Constants {
    // Subscription
    const val SUBSCRIPTION_PRODUCT_ID = "lunawell_pro_yearly"
    const val BASE_PLAN_ID = "yearly_base"

    // Notification Channels
    const val NOTIFICATION_CHANNEL_REMINDERS = "reminders_channel"
    const val NOTIFICATION_CHANNEL_PERIOD = "period_channel"

    // Database
    const val DATABASE_NAME = "lunawell_database"

    // DataStore
    const val DATASTORE_NAME = "lunawell_preferences"

    // Symptom Types
    val SYMPTOM_TYPES = listOf(
        "Cramps",
        "Headache",
        "Acne",
        "Bloating",
        "Mood Swings",
        "Fatigue",
        "Back Pain",
        "Breast Tenderness"
    )

    // Flow Intensities
    val FLOW_INTENSITIES = listOf(
        "Light",
        "Medium",
        "Heavy"
    )

    // Tracking Modes
    val TRACKING_MODES = listOf(
        "Cycle",
        "Pregnancy",
        "Wellness"
    )
}
