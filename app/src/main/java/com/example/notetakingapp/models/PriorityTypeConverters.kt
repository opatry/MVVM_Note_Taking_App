package com.example.notetakingapp.models

import androidx.room.TypeConverter

class PriorityTypeConverters {

    @TypeConverter
    fun fromPriorityToString(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun fromStringToPriority(priorityString: String): Priority {
        return Priority.valueOf(priorityString)
    }

}