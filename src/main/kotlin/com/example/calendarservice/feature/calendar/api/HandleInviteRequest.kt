package com.example.calendarservice.feature.calendar.api

import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero

data class HandleInviteRequest(
    @PositiveOrZero
    val meetupId: Long,
    @NotBlank
    val email: String,
    val status: MeetupStatus,
)
