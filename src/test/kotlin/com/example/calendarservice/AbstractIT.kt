package com.example.calendarservice

import com.example.calendarservice.AbstractIT.DockerPostgreDataSourceInitializer
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [DockerPostgreDataSourceInitializer::class])
@Testcontainers
abstract class AbstractIT {
    class DockerPostgreDataSourceInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.datasource.url=" + postgreDBContainer.jdbcUrl,
                "spring.datasource.username=" + postgreDBContainer.username,
                "spring.datasource.password=" + postgreDBContainer.password
            )
        }
    }

    companion object {
        var postgreDBContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:latest")

        init {
            postgreDBContainer.start()
        }
    }
}