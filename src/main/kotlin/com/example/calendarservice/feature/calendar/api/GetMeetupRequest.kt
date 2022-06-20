package com.example.calendarservice.feature.calendar.api

import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero

data class GetMeetupRequest(
    @NotBlank
    val requestUser: String,
    @PositiveOrZero
    val meetupId: Long,
)
