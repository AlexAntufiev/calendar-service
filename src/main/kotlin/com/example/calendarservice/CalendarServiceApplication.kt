package com.example.calendarservice

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
@ImportAutoConfiguration(JooqAutoConfiguration::class)
class CalendarServiceApplication

fun main(args: Array<String>) {
    runApplication<CalendarServiceApplication>(*args)
}
