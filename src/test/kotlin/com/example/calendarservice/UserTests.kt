package com.example.calendarservice

import com.example.calendarservice.utils.UserCreator.Companion.createUser
import com.example.calendarservice.utils.UserEmailGenerator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
class UserTests : AbstractIT() {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `create user`() {
        val email = UserEmailGenerator.generate()
        mvc.createUser(email)
            .andExpect(status().isOk)
    }

    @Test
    fun `create user - duplicate email`() {
        val email = UserEmailGenerator.generate()
        mvc.createUser(email)
            .andExpect(status().isOk)
        mvc.createUser(email)
            .andExpect(status().isInternalServerError)
    }

}
