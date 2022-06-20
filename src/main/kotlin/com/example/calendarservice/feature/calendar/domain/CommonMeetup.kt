package com.example.calendarservice.feature.calendar.domain

import java.time.OffsetDateTime

data class CommonMeetup(
    val id: Long,
    val name: String?,
    val organizer: String,
    val organizerStatus: Status,
    val guests: Map<String, Status>,
    val start: OffsetDateTime,
    val end: OffsetDateTime,
    val repeat: MeetupRepeat,
    val privacy: MeetupPrivacy,
) {
    enum class Status(
        val code: Short
    ) {
        CREATED(0),
        ACCEPTED(1),
        DECLINED(2),
        ;

        companion object {
            fun of(code: Short): Status {
                return values().single { it.code == code }
            }
        }
    }
}
