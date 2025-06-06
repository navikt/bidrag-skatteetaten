package no.nav.bidrag.reskontro

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

const val PROFILE_NAIS = "nais"
val SECURE_LOGGER: Logger = LoggerFactory.getLogger("secureLogger")
val combinedLogger: Logger = LoggerFactory.getLogger("combinedLogger")

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
class BidragReskontro

fun main(args: Array<String>) {
    SpringApplication(BidragReskontro::class.java).apply {
        setAdditionalProfiles(if (args.isEmpty()) PROFILE_NAIS else args[0])
    }.run(*args)
}
