package com.example.calendarservice.feature.user

import com.example.calendarservice.feature.user.api.CreateUserRequest
import com.example.calendarservice.feature.user.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    val userService: UserService,
) {

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest) {
        log.info("createUser: request=$request")

        userService.createUser(request.email)
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }

}