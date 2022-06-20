package com.example.calendarservice.utils

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class UserCreator {

    companion object {
        fun MockMvc.createUser(email: String) = perform(
            MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    |{
                    |   "email": "$email"
                    |}
                """.trimMargin()
                )
        )
    }
}