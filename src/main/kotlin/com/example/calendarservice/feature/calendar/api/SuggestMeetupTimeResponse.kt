package com.example.calendarservice.feature.calendar.api

import java.time.OffsetDateTime

data class SuggestMeetupTimeResponse(
    val start: OffsetDateTime,
    val end: OffsetDateTime,
)
