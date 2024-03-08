package no.nav.bidrag.reskontrolegacy.config

import no.spn.www.BisysReskWSSoapProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BisysIntegrationConfig {
    @Bean
    fun reskWsSoapProxy(@Value("\${ELIN_URL}") elinUrl: String): BisysReskWSSoapProxy {
        return BisysReskWSSoapProxy(elinUrl)
    }
}
