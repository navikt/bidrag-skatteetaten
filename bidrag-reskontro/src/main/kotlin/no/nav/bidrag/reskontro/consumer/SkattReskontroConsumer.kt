package no.nav.bidrag.reskontro.consumer

import no.nav.bidrag.commons.security.maskinporten.MaskinportenClientException
import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.reskontro.combinedLogger
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerInput
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerOutput
import no.nav.bidrag.reskontro.exceptions.IngenDataFraSkattException
import org.apache.coyote.http11.HeadersTooLargeException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class SkattReskontroConsumer(
    @param:Value("\${SKATT_URL}") private val skattUrl: String,
    @param:Qualifier("maskinporten") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "bidrag-reskontro") {
    companion object {
        const val BIDRAGSSAK_PATH = "/BisysResk/bidragssak"
        const val TRANSAKSJONER_PATH = "/BisysResk/transaksjoner"
        const val INNKREVINGSSAK_PATH = "/BisysResk/innkrevingssak"
        const val ENDRE_RM_PATH = "/BisysResk/endrerm"
    }

    fun hentInnkrevningssakerPåSak(saksnummer: Long): ReskontroConsumerOutput {
        combinedLogger.debug("Kaller hent bidragssak for sak: $saksnummer")
        var response: ResponseEntity<ReskontroConsumerOutput>? = null
        try {
            response = restTemplate.postForEntity(
                URI.create(skattUrl + BIDRAGSSAK_PATH),
                ReskontroConsumerInput(aksjonskode = 1, bidragssaksnummer = saksnummer),
                ReskontroConsumerOutput::class.java,
            )
            combinedLogger.info("Kaller hent bidragssak for sak: $saksnummer.\nFra skatt: $response.\nHeaders: ${response.headers}.")
            return validerOutput(response)
        } catch (e: HeadersTooLargeException) {
            combinedLogger.error("Response fra skatt kastet $e ved kall på sak: $saksnummer. Innhold i headers: ${response?.headers}..")
            throw e
        }
    }

    fun hentInnkrevningssakerPåPerson(person: Personident): ReskontroConsumerOutput {
        combinedLogger.debug("Kaller hent bidragssaker for person: ${person.verdi}")
        var response: ResponseEntity<ReskontroConsumerOutput>? = null
        try {
            response = restTemplate.postForEntity(
                URI.create(skattUrl + BIDRAGSSAK_PATH),
                ReskontroConsumerInput(aksjonskode = 2, fodselsOrgnr = person.verdi),
                ReskontroConsumerOutput::class.java,
            )
            combinedLogger.info("Kaller hent bidragssaker for person: ${person.verdi}.\nFra skatt: $response. \nHeaders: ${response.headers}.")
            return validerOutput(response)
        } catch (e: HeadersTooLargeException) {
            combinedLogger.error("Response fra skatt kastet $e ved kall for person: ${person.verdi}. Innhold i headers: ${response?.headers}..")
            throw e
        }
    }

    fun hentTransaksjonerPåBidragssak(saksnummer: Long): ReskontroConsumerOutput {
        combinedLogger.debug("Kaller hent transaksjoner for sak: $saksnummer")
        var response: ResponseEntity<ReskontroConsumerOutput>? = null
        try {
            response = restTemplate.postForEntity(
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
            combinedLogger.info("Kaller hent transaksjoner for sak: $saksnummer.\nFra skatt: $response.\nHeaders: ${response.headers}.")
            return validerOutput(response)
        } catch (e: HeadersTooLargeException) {
            combinedLogger.error("Response fra skatt kastet $e ved kall på transaksjoner for sak: $saksnummer. Innhold i headers: ${response?.headers}..")
            throw e
        }
    }

    fun hentTransaksjonerPåPerson(person: Personident): ReskontroConsumerOutput {
        combinedLogger.debug("Kaller hent transaksjoner for person: ${person.verdi}")
        var response: ResponseEntity<ReskontroConsumerOutput>? = null
        try {
            response = restTemplate.postForEntity(
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
            combinedLogger.info("Kaller hent transaksjoner for person: ${person.verdi}.\nFra skatt: $response.\nHeaders: ${response.headers}.")
            return validerOutput(response)
        } catch (e: HeadersTooLargeException) {
            combinedLogger.error("Response fra skatt kastet $e ved kall på transaksjoner for person: ${person.verdi}. Innhold i headers: ${response?.headers}..")
            throw e
        }
    }

    fun hentTransaksjonerPåTransaksjonsId(transaksjonsid: Long): ReskontroConsumerOutput {
        combinedLogger.debug("Kaller hent transaksjoner for transaksjonsId: $transaksjonsid")
        var response: ResponseEntity<ReskontroConsumerOutput>? = null
        try {
            response = restTemplate.postForEntity(
                URI.create(skattUrl + TRANSAKSJONER_PATH),
                ReskontroConsumerInput(
                    aksjonskode = 5,
                    transaksjonsId = transaksjonsid,
                    datoFom = "1900-01-01T00:00:00.000Z",
                    datoTom = "9999-01-01T00:00:00.000Z",
                ),
                ReskontroConsumerOutput::class.java,
            )
            combinedLogger.info("Kaller hent transaksjoner for transaksjonsId: $transaksjonsid.\nFra skatt: $response.\nHeaders: ${response.headers}.")
            return validerOutput(response)
        } catch (e: HeadersTooLargeException) {
            combinedLogger.error("Response fra skatt kastet $e ved hent transaksjoner for transaksjonsId $transaksjonsid. Innhold i headers: ${response?.headers}.")
            throw e
        }
    }

    fun hentInformasjonOmInnkrevingssaken(person: Personident): ReskontroConsumerOutput {
        combinedLogger.debug("Kaller hentInformasjonOmInnkrevingssaken for person: ${person.verdi}")
        var response: ResponseEntity<ReskontroConsumerOutput>? = null
        try {
            response = restTemplate.postForEntity(
                URI.create(skattUrl + INNKREVINGSSAK_PATH),
                ReskontroConsumerInput(aksjonskode = 6, fodselsOrgnr = person.verdi),
                ReskontroConsumerOutput::class.java,
            )
            combinedLogger.info("Response på hentInformasjonOmInnkrevingssaken for person: ${person.verdi}.\nFra skatt: $response.\nHeaders: ${response.headers}.")
            return validerOutput(response)
        } catch (e: HeadersTooLargeException) {
            combinedLogger.error("Response fra skatt kastet $e ved hentInformasjonOmInnkrevingssaken for person ${person.verdi}. Innhold i headers: ${response?.headers}.")
            throw e
        }
    }

    fun endreRmForSak(saksnummer: Long, barn: Personident, nyRm: Personident): ReskontroConsumerOutput {
        combinedLogger.debug("Kaller endre RM for sak. NyRM: ${nyRm.verdi} i sak $saksnummer med barn: ${barn.verdi}")
        val response = restTemplate.exchange(
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
        combinedLogger.info("Response på endre RM for sak: NyRM: ${nyRm.verdi} i sak $saksnummer med barn: ${barn.verdi}.\nFra skatt: $response.")
        return validerOutput(response)
    }

    /*
    Dette må gjøres siden det ikke returneres en korrekt HTTP statuskode i REST kallet mot Skatteetaten.
    Det blir derimot vedlagt en returnkode i responsen som avgjør om kallet var velykket eller ikke.
    Mulige returkoder er følgende:
        0  = OK
        -1 = Feilmelding
        -2 = Ugyldig aksjonskode - Aksjonskoden er satt individuelt for hvert endepunkt på vår side, burde ikke oppstå.
        -3 = Ingen data funnet - Tilsvarer 204 No Content.
     */
    private fun validerOutput(reskontroConsumerOutputResponse: ResponseEntity<ReskontroConsumerOutput>): ReskontroConsumerOutput {
        if (reskontroConsumerOutputResponse.statusCode == HttpStatus.UNAUTHORIZED) {
            throw MaskinportenClientException("Feil i maskinportentoken benyttet mot skatt")
        }
        if (reskontroConsumerOutputResponse.body == null) {
            error("Det mangler body i responsen fra Skatt!")
        }
        if (reskontroConsumerOutputResponse.body!!.retur == null) {
            error("Responsekoden mangler i responsen fra Skatt!")
        }

        when (reskontroConsumerOutputResponse.body?.retur?.kode) {
            0 -> return reskontroConsumerOutputResponse.body!!
            -1 -> error("Kallet mot skatt feilet med feilmelding: ${reskontroConsumerOutputResponse.body?.retur?.beskrivelse}")
            -2 -> error("Kallet mot skatt hadde ugyldig aksjonskode! Dette er ikke basert på innput og må rettes i koden/hos skatt.")
            -3 -> throw IngenDataFraSkattException("Skatt svarte med ingen data.")
            else -> error("Kallet mot skatt returnerte ukjent returnkode ${reskontroConsumerOutputResponse.body?.retur?.kode}")
        }
    }
}
