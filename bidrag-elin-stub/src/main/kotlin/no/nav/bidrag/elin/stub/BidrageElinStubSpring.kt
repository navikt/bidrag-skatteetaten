package no.nav.bidrag.elin.stub

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

const val PROFILE_NAIS = "nais"

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
class BidragElinStubSpring

fun main(args: Array<String>) {
    val app = SpringApplication(BidragElinStubSpring::class.java)
    app.setAdditionalProfiles(if (args.isEmpty()) PROFILE_NAIS else args[0])
    app.run(*args)
}
