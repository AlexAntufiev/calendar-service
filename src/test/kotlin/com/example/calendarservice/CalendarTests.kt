package com.example.calendarservice

import com.example.calendarservice.utils.UserCreator.Companion.createUser
import com.example.calendarservice.feature.calendar.api.CreateMeetupResponse
import com.example.calendarservice.utils.UserEmailGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
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
class CalendarTests : AbstractIT() {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `create meetup`() {
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
            get("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "requestUser": "$user1",
                        |   "meetupId": $meetupId
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(meetupId))
            .andExpect(jsonPath("$.name").value(meetupName))
            .andExpect(jsonPath("$.organizer").value(user1))
            .andExpect(jsonPath("$.organizerStatus").value("CREATED"))
            .andExpect(jsonPath("$.guests").isMap)
            .andExpect(jsonPath("$.guests", Matchers.hasEntry(user2, "CREATED")))
            .andExpect(jsonPath("$.guests", Matchers.hasEntry(user3, "CREATED")))
            .andExpect(jsonPath("$.start").value("2022-01-01T13:00:00+03:00"))
            .andExpect(jsonPath("$.end").value("2022-01-01T14:00:00+03:00"))
            .andExpect(jsonPath("$.repeat").value("NONE"))
    }
    @Test
    fun `create private meetup`() {
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
                        |   "guests": [ "$user2"],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "NONE",
                        |   "privacy": "PRIVATE"
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
            get("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "requestUser": "$user1",
                        |   "meetupId": $meetupId
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(meetupId))
            .andExpect(jsonPath("$.name").value(meetupName))
            .andExpect(jsonPath("$.organizer").value(user1))
            .andExpect(jsonPath("$.organizerStatus").value("CREATED"))
            .andExpect(jsonPath("$.guests").isMap)
            .andExpect(jsonPath("$.guests", Matchers.hasEntry(user2, "CREATED")))
            .andExpect(jsonPath("$.start").value("2022-01-01T13:00:00+03:00"))
            .andExpect(jsonPath("$.end").value("2022-01-01T14:00:00+03:00"))
            .andExpect(jsonPath("$.repeat").value("NONE"))

        mvc.perform(
            get("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "requestUser": "$user3",
                        |   "meetupId": $meetupId
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `create meetup - not existing email`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
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
                        |   "guests": [ "$user2" ],
                        |   "start": "$start",
                        |   "end": "$end",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isInternalServerError)
    }

    @Test
    fun `change status`() {
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
            post("/meetup/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "meetupId": ${meetupId},
                        |   "email": "$user1",
                        |   "status": "ACCEPTED"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            post("/meetup/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "meetupId": ${meetupId},
                        |   "email": "$user2",
                        |   "status": "ACCEPTED"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            post("/meetup/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "meetupId": ${meetupId},
                        |   "email": "$user3",
                        |   "status": "DECLINED"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            get("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "requestUser": "$user1",
                        |   "meetupId": $meetupId
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(meetupId))
            .andExpect(jsonPath("$.name").value(meetupName))
            .andExpect(jsonPath("$.organizer").value(user1))
            .andExpect(jsonPath("$.organizerStatus").value("ACCEPTED"))
            .andExpect(jsonPath("$.guests").isMap)
            .andExpect(jsonPath("$.guests", Matchers.hasEntry(user2, "ACCEPTED")))
            .andExpect(jsonPath("$.guests").value(Matchers.hasEntry(user3, "DECLINED")))
            .andExpect(jsonPath("$.start").value("2022-01-01T13:00:00+03:00"))
            .andExpect(jsonPath("$.end").value("2022-01-01T14:00:00+03:00"))
            .andExpect(jsonPath("$.repeat").value("NONE"))
    }

    @Test
    fun `handle invite - wrong meetupId for user`() {
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
                        |   "guests": [],
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
            post("/meetup/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "meetupId": ${meetupId},
                        |   "email": "$user2",
                        |   "status": "ACCEPTED"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isInternalServerError)
    }
}
