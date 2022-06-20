package com.example.calendarservice.feature.calendar.api

import java.time.Duration

data class SuggestMeetupTimeRequest(
    val emails: List<String>,
    val meetupDuration: Duration,
) {
    init {
        emails.forEach { require(it.isNotBlank()) }
        require(!meetupDuration.isNegative)
        require(!meetupDuration.isZero)
    }
}
