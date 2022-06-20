package com.example.calendarservice.feature.user.repository

import com.example.calendarservice.feature.user.domain.User
import com.example.calendarservice.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    val dslContext: DSLContext
) {

    fun createUser(email: String): User {
        log.info("createUser: email=$email")

        return dslContext.insertInto(USERS)
            .set(USERS.EMAIL, email)
            .returning()
            .fetchSingle()
            .let {
                User(
                    email = it.email!!,
                )
            }
            .also {
                log.info("createUser: result=$it")
            }
    }

    fun findUser(email: String): User? {
        log.info("findUser: email=$email")

        return dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(email))
            .fetchOne()
            ?.let {
                User(
                    email = it.email!!
                )
            }
            .also {
                log.info("findUser: result=$it")
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserRepository::class.java)
    }
}