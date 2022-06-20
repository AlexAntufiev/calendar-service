package com.example.calendarservice.feature.calendar.api

import java.time.OffsetDateTime

data class GetMeetupResponse(
    val id: Long,
    val name: String?,
    val organizer: String,
    val organizerStatus: MeetupStatus,
    val guests: Map<String, MeetupStatus>,
    val start: OffsetDateTime,
    val end: OffsetDateTime,
    val repeat: MeetupRepeat,
)
