package com.bypreetham.walllinki

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

class WallpaperPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    var timeSlots: List<TimeSlot>
        get() {
            val serialized = prefs.getString("time_slots", null)
            return if (serialized != null) {
                try {
                    val loaded = json.decodeFromString<List<TimeSlot>>(serialized)
                    if (loaded.size == 2 && loaded.any { it.id == "morning" } && loaded.any { it.id == "evening" }) {
                        loaded
                    } else {
                        // Re-migrate or reset to 2 slots, trying to preserve some images
                        val defaults = getDefaultSlots()
                        val morningImages = loaded.find { it.id == "morning" || it.id == "day" || it.id == "dawn" }?.imageUris ?: emptyList()
                        val eveningImages = loaded.find { it.id == "evening" || it.id == "night" || it.id == "twilight" }?.imageUris ?: emptyList()
                        
                        val updated = listOf(
                            defaults[0].copy(imageUris = morningImages),
                            defaults[1].copy(imageUris = eveningImages)
                        )
                        timeSlots = updated
                        updated
                    }
                } catch (e: Exception) {
                    getDefaultSlots()
                }
            } else {
                // Check for legacy data and migrate
                val migrated = migrateLegacyData()
                if (migrated != null) {
                    timeSlots = migrated
                    migrated
                } else {
                    val defaults = getDefaultSlots()
                    timeSlots = defaults
                    defaults
                }
            }
        }
        set(value) = prefs.edit { putString("time_slots", json.encodeToString(value)) }

    fun getActiveSlot(): TimeSlot? {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val slots = timeSlots
        
        // Check for specific matches first
        // Note: This logic assumes slots don't overlap or the first match is preferred.
        // It also handles slots that cross midnight (e.g., 22:00 to 06:00)
        return slots.find { slot ->
            if (slot.startHour < slot.endHour) {
                currentHour in slot.startHour until slot.endHour
            } else {
                // Crosses midnight
                currentHour >= slot.startHour || currentHour < slot.endHour
            }
        } ?: slots.firstOrNull() // Fallback to first slot if no match
    }

    private fun getDefaultSlots(): List<TimeSlot> {
        return listOf(
            TimeSlot("morning", "Morning", 6, 18),
            TimeSlot("evening", "Evening", 18, 6)
        )
    }

    private fun migrateLegacyData(): List<TimeSlot>? {
        val morningSet = prefs.getStringSet("morning_images", null)
        val eveningSet = prefs.getStringSet("evening_images", null)
        
        if (morningSet == null && eveningSet == null) return null

        val morningUris = morningSet?.map { Uri.parse(it) } ?: emptyList()
        val eveningUris = eveningSet?.map { Uri.parse(it) } ?: emptyList()

        return listOf(
            TimeSlot("dawn", "Dawn", 6, 8, morningUris),
            TimeSlot("day", "Day", 8, 17, morningUris),
            TimeSlot("twilight", "Twilight", 17, 19, eveningUris),
            TimeSlot("night", "Night", 19, 6, eveningUris)
        ).also {
            // Clean up legacy keys
            prefs.edit { 
                remove("morning_images")
                remove("evening_images")
            }
        }
    }

    // Deprecated helpers for compatibility during transition
    fun getCurrentImages(): List<Uri> = getActiveSlot()?.imageUris ?: emptyList()
    fun isMorning(): Boolean = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 6..17
}
