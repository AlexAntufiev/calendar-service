package com.example.calendarservice.feature.calendar.repository

import com.example.calendarservice.feature.calendar.domain.CommonMeetup
import com.example.calendarservice.feature.user.domain.User
import com.example.calendarservice.jooq.tables.references.CALENDAR
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class CalendarRepository(
    val dslContext: DSLContext
) {

    companion object {
        private val log = LoggerFactory.getLogger(CalendarRepository::class.java)
    }

    fun createCalendarDate(
        date: LocalDate,
        users: List<User>,
        meetupId: Long
    ) {
        log.info("createCalendarDate: date=$date, users=$users, meetupId=$meetupId")

        val records = users.map {
            listOf(it.email, date, CommonMeetup.Status.CREATED.code, meetupId)
        }

        dslContext.insertInto(
            CALENDAR,
            CALENDAR.EMAIL, CALENDAR.DATE, CALENDAR.STATUS, CALENDAR.MEETUP_ID
        ).apply {
            records.forEach {
                values(it)
            }
        }
            .execute()
            .apply {
                if (this != users.size) {
                    error("Can't insert rows: actual=$this, expected=${users.size}, meetupId=$meetupId")
                }
            }
    }

    fun changeStatus(email: String, meetupId: Long, status: CommonMeetup.Status) {
        log.info("createCalendarDate: email=$email, meetupId=$meetupId, status=$status")

        dslContext.update(CALENDAR)
            .set(CALENDAR.STATUS, status.code)
            .where(CALENDAR.EMAIL.eq(email).and(CALENDAR.MEETUP_ID.eq(meetupId)))
            .execute()
            .apply {
                if (this != 1) {
                    error("Can't update status: status=$status, email=$email, meetupId=$meetupId")
                }
            }
    }
}