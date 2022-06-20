package com.example.calendarservice.feature.calendar.service

import com.example.calendarservice.feature.calendar.domain.*
import com.example.calendarservice.feature.calendar.isAfterOrEqual
import com.example.calendarservice.feature.calendar.isBeforeOrEqual
import com.example.calendarservice.feature.calendar.repository.MeetupRepository
import com.example.calendarservice.feature.user.domain.User
import com.example.calendarservice.feature.user.service.UserService
import jakarta.xml.bind.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class MeetupService(
    val userService: UserService,
    val calendarService: CalendarService,
    val meetupRepository: MeetupRepository,
    val transactionTemplate: TransactionTemplate,
) {

    fun createMeetup(request: CreateMeetupRequest): Long {
        log.info("createMeetup: request=$request")

        val organizer = userService.findUser(request.organizer)
        requireNotNull(organizer) {
            "Invalid organizer id: id=${request.organizer}"
        }
        val guests = request.guests
            .map { userService.findUser(it) }

        with(guests.none { it == null }) {
            if (!this) {
                throw ValidationException("Invalid guest ids")
            }
        }

        val users = ArrayList<User>(guests as List<User>).apply {
            add(organizer)
        }
        return transactionTemplate.execute {
            val meetupId = meetupRepository.createMeetup(request)
            calendarService.createCalendarDate(
                date = request.startTime.toLocalDate(),
                users = users,
                meetupId = meetupId
            )
            meetupId
        }!!
            .also { log.info("createMeetup: result=$it") }
    }

    fun suggestMeetup(users: List<String>, meetupDuration: Duration): Pair<OffsetDateTime, OffsetDateTime> {
        log.info("suggestMeetup: users=$users, meetupDuration=$meetupDuration")

        with(users
            .map { userService.findUser(it) }
            .none { it == null }
        ) {
            require(this) {
                "Invalid guest id"
            }
        }

        var maxEndTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(DURATION_TO_NEXT_MEETUP)
        val repeatedMeetups: List<Pair<OffsetDateTime, RepeatedMeetup>> =
            meetupRepository.findNotDeclinedRepeatedMeetups(users)
                .map { Pair(movedRepeatedMeetupToDateTime(it, maxEndTime, it.repeat.convertToChrono()), it) }

        var meetupStart = maxEndTime
        var meetupEnd = maxEndTime
        var dontNeedFetchMeetups = false
        while (maxEndTime != null) {
            meetupStart = maxEndTime
            meetupEnd = meetupStart.plus(meetupDuration)
            repeatedMeetups.map {
                if (it.first.isBefore(meetupEnd)) {
                    val newStart = it.first.plus(1, it.second.repeat.convertToChrono())
                    maxEndTime = it.second.end
                    dontNeedFetchMeetups = true
                    return@map Pair(newStart, it.second)
                }
                it
            }
            if (dontNeedFetchMeetups) {
                dontNeedFetchMeetups = false
                continue
            }
            maxEndTime = meetupRepository.findMaxEndTimeFromStartTimeForUsers(users, meetupEnd)
        }
        return meetupStart to meetupEnd
            .also { log.info("suggestMeetup: result=$it") }
    }

    private fun movedRepeatedMeetupToDateTime(
        it: RepeatedMeetup,
        maxEndTime: OffsetDateTime,
        chronoUnit: ChronoUnit,
    ): OffsetDateTime {
        val until = it.start.until(maxEndTime, chronoUnit)
        return it.start.plus(until, chronoUnit)
    }

    fun getMeetup(requestUser: String, meetupId: Long): Pair<GetMeetupResult, CommonMeetup?> {
        log.info("getMeetup: requestUser=$requestUser, meetupId=$meetupId")

        val organizer = userService.findUser(requestUser)
        requireNotNull(organizer) {
            "Invalid organizer id: id=${requestUser}"
        }

        val meetup = requireNotNull(meetupRepository.findMeetup(meetupId)) {
            "Meetup is not found: meetupId=$meetupId"
        }
        return when (meetup.privacy) {
            MeetupPrivacy.PUBLIC -> GetMeetupResult.ALLOW to meetup
            MeetupPrivacy.PRIVATE -> {
                if (meetup.organizer == requestUser || meetup.guests.containsKey(requestUser)) {
                    GetMeetupResult.ALLOW to meetup
                } else {
                    GetMeetupResult.DENY to null
                }
            }
        }
            .also { log.info("getMeetup: result=$it") }
    }

    fun getMeetupIds(email: String, start: OffsetDateTime, end: OffsetDateTime): Set<Long> {
        log.info("getMeetupIds: email=$email, start=$start, end=$end")
        return meetupRepository.findNotDeclinedRepeatedMeetups(listOf(email))
            .filter {
                it.repeat == MeetupRepeat.NONE || isInclusiveMeetup(
                    meetupStart = it.start,
                    meetupEnd = it.end,
                    findStart = start,
                    findEnd = end,
                    chronoUnit = it.repeat.convertToChrono()
                )
            }
            .map { it.id }
            .toSet()
            .plus(meetupRepository.findNotDeclinedNotRepeatedMeetupIds(email, start, end))
            .also { log.info("getMeetupIds: result=$it") }
    }

    private fun isInclusiveMeetup(
        meetupStart: OffsetDateTime,
        meetupEnd: OffsetDateTime,
        findStart: OffsetDateTime,
        findEnd: OffsetDateTime,
        chronoUnit: ChronoUnit,
    ): Boolean {
        val until = meetupStart.until(findStart, chronoUnit)
        val movedMeetupStart = meetupStart.plus(until, chronoUnit)
        val meetupDuration = Duration.between(meetupEnd, meetupStart)
        if (movedMeetupStart.isAfterOrEqual(findStart) && movedMeetupStart.plus(meetupDuration)
                .isBeforeOrEqual(findEnd)
        ) {
            return true
        }
        return false
    }

    companion object {
        private val log = LoggerFactory.getLogger(MeetupService::class.java)
        private const val DURATION_TO_NEXT_MEETUP: Long = 5
    }
}
