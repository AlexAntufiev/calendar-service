package com.example.calendarservice.tech

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.validation.ValidationException

@RestControllerAdvice
class ErrorHandlingControllerAdvice {

    companion object {
        private val log = LoggerFactory.getLogger(ErrorHandlingControllerAdvice::class.java)
    }

    @ExceptionHandler(ValidationException::class)
    fun validationException(e: ValidationException): ResponseEntity<String> {
        log.warn("Can't handle bad request", e)
        return ResponseEntity.badRequest().body(e.localizedMessage)
    }

    @ExceptionHandler(Throwable::class)
    fun throwable(e: Throwable): ResponseEntity<String> {
        log.error("Can't handle request", e)
        return ResponseEntity.internalServerError().body(e.localizedMessage)
    }

}