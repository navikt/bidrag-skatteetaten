package no.nav.bidrag.regnskap

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync

const val PROFILE_NAIS = "nais"

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableAsync
class BidragRegnskap

fun main(args: Array<String>) {
    val app = SpringApplication(BidragRegnskap::class.java)
    app.setAdditionalProfiles(if (args.isEmpty()) PROFILE_NAIS else args[0])
    app.run(*args)
}
