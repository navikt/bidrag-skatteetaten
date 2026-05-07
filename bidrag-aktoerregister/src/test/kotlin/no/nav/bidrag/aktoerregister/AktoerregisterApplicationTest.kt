package no.nav.bidrag.aktoerregister

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableConfigurationProperties
class AktoerregisterApplicationTest

fun main(args: Array<String>) {
    val app = SpringApplication(AktoerregisterApplicationTest::class.java)
    app.setAdditionalProfiles("local", "nais", "lokal-nais-secrets", "lokal-nais")
    app.run(*args)
}
