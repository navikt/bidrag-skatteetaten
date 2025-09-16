package no.nav.bidrag.regnskap.consumer

import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class BidragReskontroConsumer(
    @param:Value($$"${BIDRAG_RESKONTRO_URL}") val uri: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate, "bidrag-reskontro") {
    private val bidragReskontroUri
        get() = UriComponentsBuilder.fromUri(uri)

    fun hentTransasksjonerForSak(saksnummer: String): TransaksjonerDto? {
        val postForEntity = postForEntity<ResponseEntity<TransaksjonerDto>>(
            bidragReskontroUri
                .pathSegment("bidragssak")
                .build()
                .toUri(),
            SaksnummerRequest(Saksnummer(saksnummer)),
        )
        return postForEntity?.body
    }
}
