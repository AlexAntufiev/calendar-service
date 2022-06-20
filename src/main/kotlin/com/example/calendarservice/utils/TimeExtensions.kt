package com.example.calendarservice.feature.calendar

import java.time.OffsetDateTime

fun OffsetDateTime.isAfterOrEqual(then: OffsetDateTime): Boolean {
    return isAfter(then) or isEqual(then)
}

fun OffsetDateTime.isBeforeOrEqual(then: OffsetDateTime): Boolean {
    return isBefore(then) or isEqual(then)
}