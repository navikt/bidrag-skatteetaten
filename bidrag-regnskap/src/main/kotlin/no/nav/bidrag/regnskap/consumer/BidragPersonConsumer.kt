package no.nav.bidrag.regnskap.consumer

import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.person.PersondetaljerDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI


@Service
class BidragPersonConsumer(
    @param:Value("\${PERSON_URL}") val url: URI,
    @param:Qualifier("azure") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "bidrag-aktoerregister") {

    companion object {
        private const val PERSON_PATH = "/informasjon/detaljer"
    }

    fun hentPerson(personIdent: Ident): PersondetaljerDto? {
        val personRequestUri = byggPersonRequestUri()
        val personRequest = PersonRequest(Personident(personIdent.verdi))

        return postForEntity(personRequestUri, personRequest)
    }

    private fun byggPersonRequestUri(): URI = UriComponentsBuilder.fromUri(url)
        .path(PERSON_PATH)
        .build()
        .toUri()
}
