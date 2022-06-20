package com.example.calendarservice

import com.example.calendarservice.utils.UserCreator.Companion.createUser
import com.example.calendarservice.feature.calendar.api.CreateMeetupResponse
import com.example.calendarservice.utils.UserEmailGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
class FindMeetupTests : AbstractIT() {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `find meetup - not exists`() {
        val user1 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        val meetupName = "Собеседование"
        val start = "2022-01-01T10:00:00Z"
        val end = "2022-01-01T11:00:00Z"
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2021-01-01T13:00:00+03:00",
                        |   "end": "2021-01-01T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds").isEmpty)
    }

    @Test
    fun `find meetup - not exists, cross one border`() {
        val user1 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        val meetupName = "Собеседование"
        val start = "2022-01-01T10:00:00Z"
        val end = "2022-01-01T12:00:00Z"
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2022-01-01T13:00:00+03:00",
                        |   "end": "2022-01-01T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds").isEmpty)
    }

    @Test
    fun `find meetup - exists`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2022-01-01T10:00:00Z"
        val end = "2022-01-01T11:00:00Z"
        val meetupId = mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CreateMeetupResponse::class.java) }
            .meetupId

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user2",
                        |   "start": "2022-01-01T11:00+03:00",
                        |   "end": "2022-01-01T19:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds[0]").value(meetupId))
    }

    @Test
    fun `find meetup - exists with inclusive borders`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        val meetupName = "Собеседование"
        val start = "2022-01-01T10:00:00Z"
        val end = "2022-01-01T11:00:00Z"
        val meetupId = mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CreateMeetupResponse::class.java) }
            .meetupId

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user2",
                        |   "start": "2022-01-01T13:00+03:00",
                        |   "end": "2022-01-01T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds[0]").value(meetupId))
    }

    @Test
    fun `find repeated meetup - DAILY find in organizer`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2021-01-01T13:00:00+03:00"
        val end = "2021-01-01T14:00:00+03:00"
        val meetupId = mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "DAILY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CreateMeetupResponse::class.java) }
            .meetupId

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2021-05-01T13:00:00+03:00",
                        |   "end": "2021-06-25T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds[0]").value(meetupId))
    }

    @Test
    fun `find repeated meetup - WEEKLY find in organizer`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2021-01-02T13:00:00+03:00"
        val end = "2021-01-02T14:00:00+03:00"
        val meetupId = mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "WEEKLY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CreateMeetupResponse::class.java) }
            .meetupId

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2022-01-01T13:00:00+03:00",
                        |   "end": "2022-01-01T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds[0]").value(meetupId))
    }

    @Test
    fun `find repeated meetup - MONTHLY find in organizer`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2021-01-01T13:00:00+03:00"
        val end = "2021-01-01T14:00:00+03:00"
        val meetupId = mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "MONTHLY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CreateMeetupResponse::class.java) }
            .meetupId

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2021-05-01T13:00:00+03:00",
                        |   "end": "2021-06-25T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds[0]").value(meetupId))
    }

    @Test
    fun `find repeated meetup - ANNUALY find in organizer`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2020-01-01T13:00:00+03:00"
        val end = "2020-01-01T14:00:00+03:00"
        val meetupId = mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "ANNUALLY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CreateMeetupResponse::class.java) }
            .meetupId

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2021-01-01T13:00:00+03:00",
                        |   "end": "2021-01-01T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds[0]").value(meetupId))
    }

    @Test
    fun `find repeated meetup - find in guests`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2021-01-01T13:00:00+03:00"
        val end = "2021-01-01T14:00:00+03:00"
        val meetupId = mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user2",
                        |   "guests": [ "$user1","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "MONTHLY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CreateMeetupResponse::class.java) }
            .meetupId

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2021-05-01T13:00:00+03:00",
                        |   "end": "2021-06-25T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds[0]").value(meetupId))
    }

    @Test
    fun `find repeated meetup - DAILY dont find`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2021-01-01T14:00:00+03:00"
        val end = "2021-01-01T15:00:00+03:00"
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "DAILY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2023-02-02T13:00:00+03:00",
                        |   "end": "2023-02-02T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds").isEmpty)
    }

    @Test
    fun `find repeated meetup - WEEKLY dont find`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2021-01-01T14:00:00+03:00"
        val end = "2021-01-01T15:00:00+03:00"
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "WEEKLY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2023-02-02T13:00:00+03:00",
                        |   "end": "2023-02-02T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds").isEmpty)
    }

    @Test
    fun `find repeated meetup - MONTHLY dont find`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2021-01-01T13:00:00+03:00"
        val end = "2021-01-01T14:00:00+03:00"
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "MONTHLY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2023-02-02T13:00:00+03:00",
                        |   "end": "2023-02-02T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds").isEmpty)
    }

    @Test
    fun `find repeated meetup - ANNUALY dont find`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = "2021-01-01T13:00:00+03:00"
        val end = "2021-01-01T14:00:00+03:00"
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "ANNUALLY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            get("/meetup/period")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "email": "$user1",
                        |   "start": "2023-02-02T13:00:00+03:00",
                        |   "end": "2023-02-02T14:00:00+03:00"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meetupIds").isArray)
            .andExpect(jsonPath("$.meetupIds").isEmpty)
    }
}
