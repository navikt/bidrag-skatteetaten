package no.nav.bidrag.regnskap

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@EnableAspectJAutoProxy
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableAsync
class BidragRegnskapLocal

fun main(args: Array<String>) {
    val app = SpringApplication(BidragRegnskapLocal::class.java)
    app.setAdditionalProfiles("local", "nais", "lokal-nais-secrets", "lokal-nais")
    app.run(*args)
}
