package com.example.calendarservice.feature.calendar

import com.example.calendarservice.feature.calendar.api.MeetupStatus
import com.example.calendarservice.feature.calendar.domain.CommonMeetup
import com.example.calendarservice.feature.calendar.domain.CreateMeetupRequest
import com.example.calendarservice.feature.calendar.domain.MeetupPrivacy
import com.example.calendarservice.feature.calendar.domain.MeetupRepeat

fun CommonMeetup.Status.convert(): MeetupStatus {
    return when (this) {
        CommonMeetup.Status.CREATED -> MeetupStatus.CREATED
        CommonMeetup.Status.ACCEPTED -> MeetupStatus.ACCEPTED
        CommonMeetup.Status.DECLINED -> MeetupStatus.DECLINED
    }
}

fun com.example.calendarservice.feature.calendar.api.MeetupRepeat.convert(): MeetupRepeat {
    return when (this) {
        com.example.calendarservice.feature.calendar.api.MeetupRepeat.NONE -> MeetupRepeat.NONE
        com.example.calendarservice.feature.calendar.api.MeetupRepeat.DAILY -> MeetupRepeat.DAILY
        com.example.calendarservice.feature.calendar.api.MeetupRepeat.WEEKLY -> MeetupRepeat.WEEKLY
        com.example.calendarservice.feature.calendar.api.MeetupRepeat.MONTHLY -> MeetupRepeat.MONTHLY
        com.example.calendarservice.feature.calendar.api.MeetupRepeat.ANNUALLY -> MeetupRepeat.ANNUALLY
    }
}

fun com.example.calendarservice.feature.calendar.api.MeetupPrivacy.convert(): MeetupPrivacy {
    return when (this) {
        com.example.calendarservice.feature.calendar.api.MeetupPrivacy.PRIVATE -> MeetupPrivacy.PRIVATE
        com.example.calendarservice.feature.calendar.api.MeetupPrivacy.PUBLIC -> MeetupPrivacy.PUBLIC
    }
}

fun MeetupRepeat.convert(): com.example.calendarservice.feature.calendar.api.MeetupRepeat {
    return when (this) {
        MeetupRepeat.NONE -> com.example.calendarservice.feature.calendar.api.MeetupRepeat.NONE
        MeetupRepeat.DAILY -> com.example.calendarservice.feature.calendar.api.MeetupRepeat.DAILY
        MeetupRepeat.WEEKLY -> com.example.calendarservice.feature.calendar.api.MeetupRepeat.WEEKLY
        MeetupRepeat.MONTHLY -> com.example.calendarservice.feature.calendar.api.MeetupRepeat.MONTHLY
        MeetupRepeat.ANNUALLY -> com.example.calendarservice.feature.calendar.api.MeetupRepeat.ANNUALLY
    }
}

fun com.example.calendarservice.feature.calendar.api.CreateMeetupRequest.convert() =
    CreateMeetupRequest(
        name = name,
        organizer = organizer,
        guests = guests,
        startTime = start,
        endTime = end,
        repeat = repeat.convert(),
        privacy = privacy.convert()
    )