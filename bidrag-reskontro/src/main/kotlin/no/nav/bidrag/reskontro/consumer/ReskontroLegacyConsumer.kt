package no.nav.bidrag.reskontro.consumer

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.EndreRmForSakRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakMedSkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssaksinformasjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.getForEntity
import java.net.URI

private val LOGGER = KotlinLogging.logger { }

@Service
class ReskontroLegacyConsumer(
    @param:Value("\${RESKONTRO_LEGACY_URL}") private val reskontroLegacyUrl: String,
    @param:Qualifier("azure") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "bidrag-reskontro-legacy") {

    fun hentInnkrevningssakerPåSak(saksnummerRequest: SaksnummerRequest): BidragssakDto? {
        LOGGER.info { "Kaller hent bidragssak for sak: ${saksnummerRequest.saksnummer.verdi} mot reskontro-legacy." }
        return restTemplate.postForEntity(
            URI.create("$reskontroLegacyUrl/innkrevningssak/bidragssak"),
            saksnummerRequest,
            BidragssakDto::class.java,
        ).body
    }

    fun hentInnkrevningssakerPåPerson(personRequest: PersonRequest): BidragssakMedSkyldnerDto? {
        LOGGER.info { "Kaller hent bidragssaker for person: ${personRequest.ident.verdi} mot reskontro-legacy." }
        return restTemplate.postForEntity(
            URI.create("$reskontroLegacyUrl/innkrevningssak/person"),
            personRequest,
            BidragssakMedSkyldnerDto::class.java,
        ).body
    }

    fun hentTransaksjonerPåBidragssak(saksnummerRequest: SaksnummerRequest): TransaksjonerDto? {
        LOGGER.info { "Kaller hent transaksjoner for sak: ${saksnummerRequest.saksnummer.verdi} mot reskontro-legacy." }
        return restTemplate.postForEntity(
            URI.create("$reskontroLegacyUrl/transaksjoner/bidragssak"),
            saksnummerRequest,
            TransaksjonerDto::class.java,
        ).body
    }

    fun hentTransaksjonerPåPerson(personRequest: PersonRequest): TransaksjonerDto? {
        LOGGER.info { "Kaller hent transaksjoner for person: ${personRequest.ident.verdi} mot reskontro-legacy." }
        return restTemplate.postForEntity(
            URI.create("$reskontroLegacyUrl/transaksjoner/person"),
            personRequest,
            TransaksjonerDto::class.java,
        ).body
    }

    fun hentTransaksjonerPåTransaksjonsId(transaksjonsid: Long): TransaksjonerDto? {
        LOGGER.info { "Kaller hent transaksjoner for transaksjonsId: $transaksjonsid mot reskontro-legacy." }
        return restTemplate.getForEntity<TransaksjonerDto>(
            "$reskontroLegacyUrl/transaksjoner/transaksjonsid?transaksjonsid=$transaksjonsid",
        ).body
    }

    fun hentInformasjonOmInnkrevingssaken(personRequest: PersonRequest): InnkrevingssaksinformasjonDto? {
        LOGGER.info { "Kaller hent informasjonOmInnkrevingssaken for person: ${personRequest.ident.verdi} mot reskontro-legacy." }
        return restTemplate.postForEntity(
            URI.create("$reskontroLegacyUrl/innkrevingsinformasjon"),
            personRequest,
            InnkrevingssaksinformasjonDto::class.java,
        ).body
    }

    fun endreRmForSak(endreRmForSakRequest: EndreRmForSakRequest) {
        LOGGER.info { "Kaller endre RM for sak. NyRM: ${endreRmForSakRequest.nyttFødselsnummer.verdi} i sak ${endreRmForSakRequest.saksnummer.verdi} med barn: ${endreRmForSakRequest.barn.verdi} mot reskontro-legacy." }
        restTemplate.patchForObject(
            URI.create("$reskontroLegacyUrl/endreRmForSak"),
            endreRmForSakRequest,
            Any::class.java,
        )
    }
}
