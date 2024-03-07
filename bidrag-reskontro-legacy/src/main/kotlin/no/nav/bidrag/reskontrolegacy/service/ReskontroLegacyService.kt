package no.nav.bidrag.reskontrolegacy.service

import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakMedSkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssaksinformasjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import org.springframework.stereotype.Service

@Service
class ReskontroLegacyService() {

    fun hentInnkrevingssakPåSak(saksnummerRequest: SaksnummerRequest): BidragssakDto {
        TODO("Not yet implemented")
    }

    fun hentInnkrevingssakPåPerson(personRequest: PersonRequest): BidragssakMedSkyldnerDto {
        TODO("Not yet implemented")
    }

    fun hentTransaksjonerPåBidragssak(saksnummerRequest: SaksnummerRequest): TransaksjonerDto {
        TODO("Not yet implemented")
    }

    fun hentTransaksjonerPåPerson(personRequest: PersonRequest): TransaksjonerDto {
        TODO("Not yet implemented")
    }

    fun hentTransaksjonerPåTransaksjonsid(transaksjonsid: Long): TransaksjonerDto {
        TODO("Not yet implemented")
    }

    fun hentInformasjonOmInnkrevingssaken(personRequest: PersonRequest): InnkrevingssaksinformasjonDto {
        TODO("Not yet implemented")
    }

    fun endreRmForSak(saksnummer: Saksnummer, barn: Personident, nyttFødselsnummer: Personident) {
        TODO("Not yet implemented")
    }
}
