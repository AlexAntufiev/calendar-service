package com.example.calendarservice.feature.user.service

import com.example.calendarservice.feature.user.domain.User
import com.example.calendarservice.feature.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository,
) {

    fun createUser(email: String) {
        log.info("createUser: email=$email")

        userRepository.createUser(email)
    }

    fun findUser(email: String): User? {
        log.info("findUser: email=$email")

        return userRepository.findUser(email)
            .also {
                log.info("findUser: result=$it")
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }
}