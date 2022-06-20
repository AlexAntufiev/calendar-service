package com.example.calendarservice.feature.calendar.repository

import com.example.calendarservice.feature.calendar.domain.*
import com.example.calendarservice.jooq.tables.records.MeetupRecord
import com.example.calendarservice.jooq.tables.references.CALENDAR
import com.example.calendarservice.jooq.tables.references.MEETUP
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Result
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class MeetupRepository(
    val dslContext: DSLContext
) {
    fun createMeetup(request: CreateMeetupRequest): Long {
        log.info("createMeetup: request=$request")
        return dslContext.insertInto(MEETUP)
            .set(MEETUP.NAME, request.name)
            .set(MEETUP.ORGANIZER, request.organizer)
            .set(MEETUP.GUESTS, request.guests.toTypedArray())
            .set(MEETUP.START_TIME, request.startTime)
            .set(MEETUP.END_TIME, request.endTime)
            .set(MEETUP.REPEAT, request.repeat.code)
            .set(MEETUP.PRIVACY, request.privacy.code)
            .returning(MEETUP.ID)
            .fetchSingle(MEETUP.ID)!!
            .also { log.info("createMeetup: result=$it") }
    }

    fun findMeetup(meetupId: Long): CommonMeetup? {
        log.info("findMeetup: meetupId=$meetupId")
        return dslContext.select()
            .from(MEETUP)
            .join(CALENDAR)
            .on(MEETUP.ID.eq(CALENDAR.MEETUP_ID))
            .where(MEETUP.ID.eq(meetupId))
            .fetch()
            .let { meetup(it) }
            .also { log.info("findMeetup: result=$it") }
    }

    private fun meetup(result: Result<Record>): CommonMeetup? {
        if (result.isEmpty()) {
            return null
        }
        val meetup: MeetupRecord = result.into(MEETUP).first()
        val emailToStatus = result.associateBy(keySelector = { it.into(CALENDAR).email!! },
            valueTransform = { CommonMeetup.Status.of(it.into(CALENDAR).status!!) })
        val organizer = meetup.organizer!!
        val organizerStatus = emailToStatus[organizer]!!
        emailToStatus.minus(organizer)
        return CommonMeetup(
            id = meetup.id!!,
            name = meetup.name,
            organizer = organizer,
            organizerStatus = organizerStatus,
            guests = emailToStatus,
            start = meetup.startTime!!,
            end = meetup.endTime!!,
            repeat = MeetupRepeat.of(meetup.repeat!!),
            privacy = MeetupPrivacy.of(meetup.privacy!!),
        )
    }

    fun findNotDeclinedNotRepeatedMeetupIds(email: String, start: OffsetDateTime, end: OffsetDateTime): List<Long> {
        log.info("findNotDeclinedNotRepeatedMeetupIds: email=$email, start=$start, end=$end")
        return dslContext.select()
            .from(MEETUP)
            .join(CALENDAR)
            .on(MEETUP.ID.eq(CALENDAR.MEETUP_ID))
            .where(CALENDAR.EMAIL.eq(email))
            .and(CALENDAR.STATUS.ne(CommonMeetup.Status.DECLINED.code))
            .and(MEETUP.START_TIME.ge(start).and(MEETUP.END_TIME.le(end)))
            .and(MEETUP.REPEAT.eq(MeetupRepeat.NONE.code))
            .fetch()
            .map {
                val meetupRecord = it.into(MEETUP)
                meetupRecord.id!!
            }
            .also { log.info("findNotDeclinedNotRepeatedMeetupIds: result=$it") }
    }

    fun findNotDeclinedRepeatedMeetups(emails: List<String>): List<RepeatedMeetup> {
        log.info("findNotDeclinedRepeatedMeetups: emails=$emails")
        return dslContext.select()
            .distinctOn(CALENDAR.MEETUP_ID)
            .from(MEETUP)
            .join(CALENDAR)
            .on(MEETUP.ID.eq(CALENDAR.MEETUP_ID))
            .where(CALENDAR.EMAIL.`in`(emails))
            .and(CALENDAR.STATUS.ne(CommonMeetup.Status.DECLINED.code))
            .and(MEETUP.REPEAT.ne(MeetupRepeat.NONE.code))
            .fetch()
            .map {
                val meetupRecord = it.into(MEETUP)
                RepeatedMeetup(
                    id = meetupRecord.id!!,
                    start = meetupRecord.startTime!!,
                    end = meetupRecord.endTime!!,
                    repeat = MeetupRepeat.of(meetupRecord.repeat!!)
                )
            }
            .also { log.info("findNotDeclinedRepeatedMeetups: result=$it") }

    }

    fun findMaxEndTimeFromStartTimeForUsers(users: List<String>, startTime: OffsetDateTime): OffsetDateTime? {
        log.info("findMaxEndTimeFromStartTimeForUsers: users=$users, startTime=$startTime")
        return dslContext.select(DSL.max(MEETUP.END_TIME))
            .from(CALENDAR)
            .join(MEETUP)
            .on(MEETUP.ID.eq(CALENDAR.MEETUP_ID))
            .where(CALENDAR.EMAIL.`in`(users))
            .and(CALENDAR.STATUS.ne(CommonMeetup.Status.DECLINED.code))
            .and(MEETUP.END_TIME.ge(startTime))
            .fetchOne(DSL.max(MEETUP.END_TIME))
            .also { log.info("findMaxEndTimeFromStartTimeForUsers: result=$it") }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MeetupRepository::class.java)
    }
}