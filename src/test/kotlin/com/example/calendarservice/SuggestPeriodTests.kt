package com.example.calendarservice

import com.example.calendarservice.utils.UserCreator.Companion.createUser
import com.example.calendarservice.feature.calendar.api.SuggestMeetupTimeResponse
import com.example.calendarservice.utils.UserEmailGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset


@SpringBootTest
@AutoConfigureMockMvc
class SuggestPeriodTests : AbstractIT() {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `suggest period - from now`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "2022-01-01T10:00:00Z",
                        |   "end": "2022-01-01T11:00:00Z",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        val start = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5)
        val durationAsString = "PT10M"
        val response = mvc.perform(
            get("/meetup/suggest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "emails": [ "$user1", "$user2", "$user3"],
                        |   "meetupDuration": "$durationAsString"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, SuggestMeetupTimeResponse::class.java) }

        Assertions.assertTrue(Duration.between(start, response.start).toMinutes() == 0L)
        Assertions.assertTrue(
            Duration.between(start.plus(Duration.parse(durationAsString)), response.end).toMinutes() == 0L
        )
    }

    @Test
    fun `suggest period - after meetup`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val start = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5)
        val end = start.plusHours(7)
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
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        val durationAsString = "PT1H"
        val response = mvc.perform(
            get("/meetup/suggest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "emails": [ "$user1", "$user2", "$user3"],
                        |   "meetupDuration": "$durationAsString"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, SuggestMeetupTimeResponse::class.java) }

        Assertions.assertTrue(Duration.between(end, response.start).toMinutes() == 0L)
        Assertions.assertTrue(
            Duration.between(response.start.plus(Duration.parse(durationAsString)), response.end).toMinutes() == 0L
        )
    }

    @Test
    fun `suggest period - after several meetups`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val pastStart = "2022-01-01T10:00:00+03:00"
        val pastEnd = "2022-01-01T11:00:00+03:00"
        val nowStart = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5)
        val nowEnd = nowStart.plusHours(7)
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [],
                        |   "start": "$pastStart",
                        |   "end": "$pastEnd",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [],
                        |   "start": "$pastEnd",
                        |   "end": "$nowStart",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user2",
                        |   "guests": [ "$user3" ],
                        |   "start": "$nowStart",
                        |   "end": "$nowEnd",
                        |   "repeat": "NONE",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        val durationAsString = "PT1H"
        val response = mvc.perform(
            get("/meetup/suggest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "emails": [ "$user1", "$user2", "$user3"],
                        |   "meetupDuration": "$durationAsString"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, SuggestMeetupTimeResponse::class.java) }

        Assertions.assertTrue(Duration.between(nowEnd, response.start).toMinutes() == 0L)
        Assertions.assertTrue(
            Duration.between(response.start.plus(Duration.parse(durationAsString)), response.end).toMinutes() == 0L
        )
    }

    @Test
    fun `suggest period - after repeated meetup`() {
        val user1 = UserEmailGenerator.generate()
        val user2 = UserEmailGenerator.generate()
        val user3 = UserEmailGenerator.generate()
        mvc.createUser(user1)
        mvc.createUser(user2)
        mvc.createUser(user3)
        val meetupName = "Собеседование"
        val pastStart = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(1).plusMinutes(5)
        val pastEnd = pastStart.plusHours(2)
        mvc.perform(
            post("/meetup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "name": "$meetupName",
                        |   "organizer": "$user1",
                        |   "guests": [ "$user2","$user3"],
                        |   "start": "$pastStart",
                        |   "end": "$pastEnd",
                        |   "repeat": "DAILY",
                        |   "privacy": "PUBLIC"
                        |}
                    """.trimMargin()
                )
        ).andExpect(status().isOk)

        val durationAsString = "PT1H"
        val response = mvc.perform(
            get("/meetup/suggest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        |{
                        |   "emails": [ "$user1", "$user2", "$user3"],
                        |   "meetupDuration": "$durationAsString"
                        |}
                    """.trimMargin()
                )
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, SuggestMeetupTimeResponse::class.java) }

        Assertions.assertTrue(Duration.between(response.start.toOffsetTime(), pastEnd.toOffsetTime()).toMinutes() == 0L)
        Assertions.assertTrue(
            Duration.between(response.start.plus(Duration.parse(durationAsString)), response.end).toMinutes() == 0L
        )

    }
}
