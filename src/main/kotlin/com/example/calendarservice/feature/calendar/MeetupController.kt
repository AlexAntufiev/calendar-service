package com.example.calendarservice.feature.calendar

import com.example.calendarservice.feature.calendar.api.*
import com.example.calendarservice.feature.calendar.domain.CommonMeetup
import com.example.calendarservice.feature.calendar.domain.GetMeetupResult
import com.example.calendarservice.feature.calendar.service.CalendarService
import com.example.calendarservice.feature.calendar.service.MeetupService
import jakarta.xml.bind.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.example.calendarservice.feature.calendar.api.CreateMeetupRequest as ApiCreateMeetupRequest

@RestController
@RequestMapping("/meetup")
class MeetupController(
    val meetupService: MeetupService,
    val calendarService: CalendarService,
) {

    @PostMapping
    fun createMeetup(@RequestBody request: ApiCreateMeetupRequest): CreateMeetupResponse {
        log.info("createMeetup: request=$request")
        return CreateMeetupResponse(meetupId = meetupService.createMeetup(request.convert()))
            .also { log.info("createMeetup: result=$it") }
    }

    @GetMapping
    fun getMeetup(@RequestBody request: GetMeetupRequest): ResponseEntity<GetMeetupResponse> {
        log.info("getMeetup: request=$request")
        val meetup = meetupService.getMeetup(request.requestUser, request.meetupId)
        return when (meetup.first) {
            GetMeetupResult.ALLOW -> with(meetup.second!!) {
                GetMeetupResponse(
                    id = id,
                    name = name,
                    organizer = organizer,
                    organizerStatus = organizerStatus.convert(),
                    guests = guests.mapValues { it.value.convert() },
                    start = start,
                    end = end,
                    repeat = repeat.convert(),
                )
                    .let { ResponseEntity.ok(it) }
            }
            GetMeetupResult.DENY -> ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build()
        }
            .also { log.info("getMeetup: result=$it") }
    }

    @PostMapping("/invite")
    fun changeStatus(@RequestBody request: HandleInviteRequest) {
        log.info("changeStatus: request=$request")
        val status = when (request.status) {
            MeetupStatus.ACCEPTED -> CommonMeetup.Status.ACCEPTED
            MeetupStatus.DECLINED -> CommonMeetup.Status.DECLINED
            MeetupStatus.CREATED -> throw ValidationException("Invalid meetup status")
        }
        calendarService.changeStatus(
            email = request.email,
            meetupId = request.meetupId,
            status = status
        )
    }

    @GetMapping("/suggest")
    fun suggestMeetup(@RequestBody request: SuggestMeetupTimeRequest): SuggestMeetupTimeResponse {
        log.info("suggestMeetupTime: request=$request")
        val (start, end) = meetupService.suggestMeetup(request.emails, request.meetupDuration)
        return SuggestMeetupTimeResponse(
            start = start,
            end = end,
        )
            .also { log.info("suggestMeetup: result=$it") }
    }

    @GetMapping("/period")
    fun getMeetupsByPeriod(@RequestBody request: GetMeetupsByPeriodRequest): GetMeetupsByPeriodResponse {
        log.info("getMeetupsByPeriod: request=$request")
        return GetMeetupsByPeriodResponse(
            meetupIds = meetupService.getMeetupIds(
                email = request.email,
                start = request.start,
                end = request.end,
            )
        )
            .also { log.info("getMeetupsByPeriod: result=$it") }

    }

    companion object {
        private val log = LoggerFactory.getLogger(MeetupController::class.java)
    }

}
