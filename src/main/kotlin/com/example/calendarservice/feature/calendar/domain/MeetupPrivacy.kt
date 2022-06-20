package com.example.calendarservice.feature.calendar.domain

enum class MeetupPrivacy(
    val code: Short
) {
    PUBLIC(0),
    PRIVATE(1),
    ;

    companion object {
        fun of(code: Short): MeetupPrivacy {
            return MeetupPrivacy.values().single { it.code == code }
        }
    }
}