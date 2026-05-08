package no.nav.bidrag.reskontro

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration

const val PROFILE_NAIS = "nais"

@SpringBootApplication(exclude = [ServletWebSecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
class BidragReskontro

fun main(args: Array<String>) {
    SpringApplication(BidragReskontro::class.java).apply {
        setAdditionalProfiles(if (args.isEmpty()) PROFILE_NAIS else args[0])
    }.run(*args)
}
