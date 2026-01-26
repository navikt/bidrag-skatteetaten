package no.nav.bidrag.aktoerregister

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
class AktoerregisterApplication

fun main(args: Array<String>) {
    SpringApplication.run(AktoerregisterApplication::class.java, *args)
}
