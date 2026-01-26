package no.nav.bidrag.elin.stub

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
class BidragElinStubLocal

fun main(args: Array<String>) {
    val app = SpringApplication(
        BidragElinStubLocal::class.java,
    )
    app.setAdditionalProfiles("local", "nais")
    app.run(*args)
}
