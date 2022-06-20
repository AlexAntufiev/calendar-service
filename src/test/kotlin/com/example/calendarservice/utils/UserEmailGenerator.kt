package com.example.calendarservice.utils

import java.util.concurrent.atomic.AtomicLong

class UserEmailGenerator {
    companion object {
        private val userInc = AtomicLong(0)
        fun generate() = "user${userInc.getAndIncrement()}@mail.ru"
    }
}