package com.example.calendarservice.feature.calendar.domain

import java.time.temporal.ChronoUnit

enum class MeetupRepeat(
    val code: Short
) {
    NONE(0),
    DAILY(1),
    WEEKLY(2),
    MONTHLY(3),
    ANNUALLY(4),
    ;

    companion object {
        fun of(code: Short): MeetupRepeat {
            return MeetupRepeat.values().single { it.code == code }
        }
    }
}

fun MeetupRepeat.convertToChrono(): ChronoUnit {
    return when (this) {
        MeetupRepeat.NONE -> throw UnsupportedOperationException()
        MeetupRepeat.DAILY -> ChronoUnit.DAYS
        MeetupRepeat.WEEKLY -> ChronoUnit.WEEKS
        MeetupRepeat.MONTHLY -> ChronoUnit.MONTHS
        MeetupRepeat.ANNUALLY -> ChronoUnit.YEARS
    }
}