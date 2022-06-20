package com.example.calendarservice.feature.user.api

import javax.validation.constraints.NotBlank

data class CreateUserRequest(
    @NotBlank
    val email: String,
)
