package no.nav.bidrag.reskontro.service

import no.nav.bidrag.reskontro.consumer.ReskontroLegacyConsumer
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.EndreRmForSakRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakMedSkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssaksinformasjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

/**
 * Denne klassen sørger for at reskontro legacy blir kalt for å hente ut reskontro informasjon fra gamle soap tjenesten.
 * Skal deprekeres så fort nytt rest-grensesnitt er implementert hos skatt.
 */
@Service
class ReskontroLegacyService(
    private val reskontroLegacyConsumer: ReskontroLegacyConsumer,
) {

    fun hentInnkrevingssakPåSak(saksnummerRequest: SaksnummerRequest): ResponseEntity<BidragssakDto?> {
        return reskontroLegacyConsumer.hentInnkrevningssakerPåSak(saksnummerRequest)
    }

    fun hentInnkrevingssakPåPerson(personRequest: PersonRequest): ResponseEntity<BidragssakMedSkyldnerDto?> {
        return reskontroLegacyConsumer.hentInnkrevningssakerPåPerson(personRequest)
    }

    fun hentTransaksjonerPåBidragssak(saksnummerRequest: SaksnummerRequest): ResponseEntity<TransaksjonerDto?> {
        return reskontroLegacyConsumer.hentTransaksjonerPåBidragssak(saksnummerRequest)
    }

    fun hentTransaksjonerPåPerson(personRequest: PersonRequest): ResponseEntity<TransaksjonerDto?> {
        return reskontroLegacyConsumer.hentTransaksjonerPåPerson(personRequest)
    }

    fun hentTransaksjonerPåTransaksjonsid(transaksjonsid: Long): ResponseEntity<TransaksjonerDto?> {
        return reskontroLegacyConsumer.hentTransaksjonerPåTransaksjonsId(transaksjonsid)
    }

    fun hentInformasjonOmInnkrevingssaken(personRequest: PersonRequest): ResponseEntity<InnkrevingssaksinformasjonDto?> {
        return reskontroLegacyConsumer.hentInformasjonOmInnkrevingssaken(personRequest)
    }

    fun endreRmForSak(endreRmForSakRequest: EndreRmForSakRequest) {
        reskontroLegacyConsumer.endreRmForSak(endreRmForSakRequest)
    }
}
