package com.example.calendarservice.feature.calendar.api

import com.example.calendarservice.feature.calendar.isBeforeOrEqual
import java.time.OffsetDateTime
import javax.validation.constraints.NotBlank

data class GetMeetupsByPeriodRequest(
    @NotBlank
    val email: String,
    val start: OffsetDateTime,
    val end: OffsetDateTime,
) {
    init {
        require(start.isBeforeOrEqual(end))
    }
}
