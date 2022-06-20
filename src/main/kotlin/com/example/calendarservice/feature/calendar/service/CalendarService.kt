package com.example.calendarservice.feature.calendar.service

import com.example.calendarservice.feature.calendar.repository.CalendarRepository
import com.example.calendarservice.feature.calendar.domain.CommonMeetup
import com.example.calendarservice.feature.user.domain.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class CalendarService(
    val calendarRepository: CalendarRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(CalendarService::class.java)
    }

    fun createCalendarDate(
        date: LocalDate,
        users: List<User>,
        meetupId: Long
    ) {
        log.info("createCalendarDate: date=$date, users=$users, meetupId=$meetupId")

        calendarRepository.createCalendarDate(
            date = date,
            users = users,
            meetupId = meetupId
        )
    }

    fun changeStatus(email: String, meetupId: Long, status: CommonMeetup.Status) {
        log.info("createCalendarDate: email=$email, meetupId=$meetupId, status=$status")

        calendarRepository.changeStatus(
            email = email,
            meetupId = meetupId,
            status = status
        )
    }
}