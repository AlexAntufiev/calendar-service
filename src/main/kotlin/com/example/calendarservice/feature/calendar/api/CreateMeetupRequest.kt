package com.example.calendarservice.feature.calendar.api

import com.example.calendarservice.feature.calendar.isBeforeOrEqual
import java.time.OffsetDateTime
import javax.validation.constraints.NotBlank

data class CreateMeetupRequest(
    val name: String?,
    @NotBlank
    val organizer: String,
    val guests: List<String>,
    val start: OffsetDateTime,
    val end: OffsetDateTime,
    val repeat: MeetupRepeat,
    val privacy: MeetupPrivacy,
) {
    init {
        require(start.isBeforeOrEqual(end))
        name?.apply {
            require(name.isNotBlank())
        }
        guests.forEach { require(it.isNotBlank()) }
    }
}
