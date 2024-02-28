package no.nav.bidrag.reskontro.consumer

import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.reskontro.SECURE_LOGGER
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerInput
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerOutput
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class SkattReskontroConsumer(
    @Value("\${SKATT_URL}") private val skattUrl: String,
    @Qualifier("maskinporten") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "bidrag-reskontro") {
    companion object {
        const val BIDRAGSSAK_PATH = "/BisysResk/bidragssak"
        const val TRANSAKSJONER_PATH = "/BisysResk/transaksjoner"
        const val INNKREVINGSSAK_PATH = "/BisysResk/innkrevingssak"
        const val ENDRE_RM_PATH = "/BisysResk/endrerm"
    }

    fun hentInnkrevningssakerPåSak(saksnummer: Long): ResponseEntity<ReskontroConsumerOutput> {
        SECURE_LOGGER.info("Kaller hent bidragssak for sak: $saksnummer")
        return restTemplate.postForEntity(
            URI.create(skattUrl + BIDRAGSSAK_PATH),
            ReskontroConsumerInput(aksjonskode = 1, bidragssaksnummer = saksnummer),
            ReskontroConsumerOutput::class.java,
        )
    }

    fun hentInnkrevningssakerPåPerson(person: Personident): ResponseEntity<ReskontroConsumerOutput> {
        SECURE_LOGGER.info("Kaller hent bidragssaker for person: ${person.verdi}")
        return restTemplate.postForEntity(
            URI.create(skattUrl + BIDRAGSSAK_PATH),
            ReskontroConsumerInput(aksjonskode = 2, fodselsOrgnr = person.verdi),
            ReskontroConsumerOutput::class.java,
        )
    }

    fun hentTransaksjonerPåBidragssak(saksnummer: Long): ResponseEntity<ReskontroConsumerOutput> {
        SECURE_LOGGER.info("Kaller hent transaksjoner for sak: $saksnummer")
        return restTemplate.postForEntity(
            URI.create(skattUrl + TRANSAKSJONER_PATH),
            ReskontroConsumerInput(
                aksjonskode = 3,
                bidragssaksnummer = saksnummer,
                datoFom = "1900-01-01T00:00:00.000Z",
                datoTom = "9999-01-01T00:00:00.000Z",
                maxAntallTransaksjoner = Int.MAX_VALUE,
            ),
            ReskontroConsumerOutput::class.java,
        )
    }

    fun hentTransaksjonerPåPerson(person: Personident): ResponseEntity<ReskontroConsumerOutput> {
        SECURE_LOGGER.info("Kaller hent transaksjoner for person: ${person.verdi}")
        return restTemplate.postForEntity(
            URI.create(skattUrl + TRANSAKSJONER_PATH),
            ReskontroConsumerInput(
                aksjonskode = 4,
                fodselsOrgnr = person.verdi,
                datoFom = "1900-01-01T00:00:00.000Z",
                datoTom = "9999-01-01T00:00:00.000Z",
                maxAntallTransaksjoner = Int.MAX_VALUE,
            ),
            ReskontroConsumerOutput::class.java,
        )
    }

    fun hentTransaksjonerPåTransaksjonsId(transaksjonsid: Long): ResponseEntity<ReskontroConsumerOutput> {
        SECURE_LOGGER.info("Kaller hent transaksjoner for transaksjonsId: $transaksjonsid")
        return restTemplate.postForEntity(
            URI.create(skattUrl + TRANSAKSJONER_PATH),
            ReskontroConsumerInput(
                aksjonskode = 5,
                transaksjonsId = transaksjonsid,
                datoFom = "1900-01-01T00:00:00.000Z",
                datoTom = "9999-01-01T00:00:00.000Z",
            ),
            ReskontroConsumerOutput::class.java,
        )
    }

    fun hentInformasjonOmInnkrevingssaken(person: Personident): ResponseEntity<ReskontroConsumerOutput> {
        SECURE_LOGGER.info("Kaller hent informasjonOmInnkrevingssaken for person: ${person.verdi}")
        return restTemplate.postForEntity(
            URI.create(skattUrl + INNKREVINGSSAK_PATH),
            ReskontroConsumerInput(aksjonskode = 6, fodselsOrgnr = person.verdi),
            ReskontroConsumerOutput::class.java,
        )
    }

    fun endreRmForSak(saksnummer: Long, barn: Personident, nyRm: Personident): ResponseEntity<ReskontroConsumerOutput> {
        SECURE_LOGGER.info("Kaller endre RM for sak. NyRM: ${nyRm.verdi} i sak $saksnummer med barn: ${barn.verdi}")
        return restTemplate.exchange(
            URI.create(skattUrl + ENDRE_RM_PATH),
            HttpMethod.PATCH,
            HttpEntity<ReskontroConsumerInput>(
                ReskontroConsumerInput(
                    aksjonskode = 8,
                    bidragssaksnummer = saksnummer,
                    fodselsnrGjelder = barn.verdi,
                    fodselsnrNy = nyRm.verdi,
                ),
            ),
            ReskontroConsumerOutput::class.java,
        )
    }
}
