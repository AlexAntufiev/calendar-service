package com.example.calendarservice.feature.calendar.domain

import java.time.OffsetDateTime

data class RepeatedMeetup(
    val id: Long,
    val start: OffsetDateTime,
    val end: OffsetDateTime,
    val repeat: MeetupRepeat,
)