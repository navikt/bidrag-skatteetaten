package no.nav.bidrag.reskontrolegacy.config

import no.nav.bidrag.reskontrolegacy.reskws.ReskWsClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.oxm.jaxb.Jaxb2Marshaller

@Configuration
class ReskWsConfiguration {

    @Bean
    fun jaxb2marshaller(): Jaxb2Marshaller {
        return Jaxb2Marshaller().apply {
            contextPath = "no.nav.bidrag.reskontrolegacy.generated"
        }
    }

    @Bean
    fun reskWsClient(@Value("\${ELIN_URL}") elinUrl: String, jaxb2marshaller: Jaxb2Marshaller): ReskWsClient {
        return ReskWsClient().apply {
            defaultUri = elinUrl
            marshaller = jaxb2marshaller
            unmarshaller = jaxb2marshaller
        }
    }
}
