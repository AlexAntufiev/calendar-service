package com.example.calendarservice.feature.calendar.domain

import java.time.OffsetDateTime

data class CreateMeetupRequest(
    val name: String?,
    val organizer: String,
    val guests: List<String>,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
    val repeat: MeetupRepeat,
    val privacy: MeetupPrivacy,
)
