package no.nav.bidrag.reskontro.service

import no.nav.bidrag.reskontro.consumer.ReskontroLegacyConsumer
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.EndreRmForSakRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakMedSkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssaksinformasjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import org.springframework.stereotype.Service

/**
 * Denne klassen sørger for at reskontro legacy blir kalt for å hente ut reskontro informasjon fra gamle soap tjenesten.
 * Skal deprekeres så fort nytt rest-grensesnitt er implementert hos skatt.
 */
@Service
class ReskontroLegacyService(
    private val reskontroLegacyConsumer: ReskontroLegacyConsumer,
) {

    fun hentInnkrevingssakPåSak(saksnummerRequest: SaksnummerRequest): BidragssakDto? = reskontroLegacyConsumer.hentInnkrevningssakerPåSak(saksnummerRequest)

    fun hentInnkrevingssakPåPerson(personRequest: PersonRequest): BidragssakMedSkyldnerDto? = reskontroLegacyConsumer.hentInnkrevningssakerPåPerson(personRequest)

    fun hentTransaksjonerPåBidragssak(saksnummerRequest: SaksnummerRequest): TransaksjonerDto? = reskontroLegacyConsumer.hentTransaksjonerPåBidragssak(saksnummerRequest)

    fun hentTransaksjonerPåPerson(personRequest: PersonRequest): TransaksjonerDto? = reskontroLegacyConsumer.hentTransaksjonerPåPerson(personRequest)

    fun hentTransaksjonerPåTransaksjonsid(transaksjonsid: Long): TransaksjonerDto? = reskontroLegacyConsumer.hentTransaksjonerPåTransaksjonsId(transaksjonsid)

    fun hentInformasjonOmInnkrevingssaken(personRequest: PersonRequest): InnkrevingssaksinformasjonDto? = reskontroLegacyConsumer.hentInformasjonOmInnkrevingssaken(personRequest)

    fun endreRmForSak(endreRmForSakRequest: EndreRmForSakRequest) {
        reskontroLegacyConsumer.endreRmForSak(endreRmForSakRequest)
    }
}
