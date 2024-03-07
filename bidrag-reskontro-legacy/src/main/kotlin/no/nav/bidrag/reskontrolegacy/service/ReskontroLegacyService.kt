package no.nav.bidrag.reskontrolegacy.service

import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.Datoperiode
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakMedSkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.SaksinformasjonBarnDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.SkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssaksinformasjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import no.spn.www.BisysReskWSSoapProxy
import no.spn.www.CResknObjectHolder
import no.spn.www.Cretur
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

@Service
class ReskontroLegacyService(
    private val reskWSSoapProxy: BisysReskWSSoapProxy,
) {

    fun hentInnkrevingssakPåSak(saksnummerRequest: SaksnummerRequest): BidragssakDto {
        val respons = reskWSSoapProxy.rohPrSakPrBarn(
            1,
            saksnummerRequest.saksnummer.verdi.toInt(),
        )
        validerRespons(respons, "rohPrSakPrBarn")
        val bidragSak = respons._UtParameter._ColCbidragSak.cbidragSak[0]
        return BidragssakDto(
            saksnummer = Saksnummer(bidragSak._NBidragsSaksnr.toString()),
            bmGjeldFastsettelsesgebyr = bidragSak._DbmGjeldFastsGebyr,
            bpGjeldFastsettelsesgebyr = bidragSak._DbpGjeldFastGebyr,
            bmGjeldRest = bidragSak._DbmGjeldRest,
            barn = bidragSak._ColCbarnISak.cbarnISak.map {
                SaksinformasjonBarnDto(
                    personident = Personident(it._SFnr),
                    restGjeldOffentlig = it._DbpBelGjeldOff,
                    restGjeldPrivat = it._DbpBelGjeldPrivTot,
                    // TODO(Må sjekke opp om det under er riktig felt for denne verdien)
                    sumIkkeUtbetalt = it._DbidUtbetTot,
                    sumForskuddUtbetalt = it._DforskUtbetTot,
                    periode = Datoperiode(
                        LocalDateTime.ofInstant(
                            it._DtbidPerSisteDatoFom.toInstant(),
                            it._DtbidPerSisteDatoFom.getTimeZone().toZoneId(),
                        ).toLocalDate(),
                        LocalDateTime.ofInstant(
                            it._DtbidPerSisteDatoTom.toInstant(),
                            it._DtbidPerSisteDatoTom.getTimeZone().toZoneId(),
                        ).toLocalDate(),
                    ),
                    erStoppIUtbetaling = "J" == it._SstoppFord,
                )
            },
        )
    }

    fun hentInnkrevingssakPåPerson(personRequest: PersonRequest): BidragssakMedSkyldnerDto {
        val respons = reskWSSoapProxy.rohPrPersPrSakPrBarn(
            2,
            personRequest.ident.verdi,
        )
        validerRespons(respons, "rohPrPersPrSakPrBarn")
        val bidragSak = respons._UtParameter._ColCbidragSak.cbidragSak[0]
        val skyldner = respons._UtParameter._ColCpersOrg.cpersOrg[0]
        return BidragssakMedSkyldnerDto(
            skyldner = SkyldnerDto(
                personident = Personident(skyldner._Sfnr),
                innbetaltBeløpUfordelt = skyldner._DinnbetBelopUford,
                gjeldIlagtGebyr = skyldner._DgjeldGebyrIlagtTI,
            ),
            bidragssak = BidragssakDto(
                saksnummer = Saksnummer(bidragSak._NBidragsSaksnr.toString()),
                bmGjeldFastsettelsesgebyr = bidragSak._DbmGjeldFastsGebyr,
                bpGjeldFastsettelsesgebyr = bidragSak._DbpGjeldFastGebyr,
                bmGjeldRest = bidragSak._DbmGjeldRest,
                barn = bidragSak._ColCbarnISak.cbarnISak.map {
                    SaksinformasjonBarnDto(
                        personident = Personident(it._SFnr),
                        restGjeldOffentlig = it._DbpBelGjeldOff,
                        restGjeldPrivat = it._DbpBelGjeldPrivTot,
                        sumForskuddUtbetalt = it._DforskUtbetTot,
                        restGjeldPrivatAndel = it._DbpBelGjeldPrivAndel,
                        sumInnbetaltAndel = it._DbidUtbetAndel,
                        sumForskuddUtbetaltAndel = it._DforskUtbetAndel,
                    )
                },
            ),
        )
    }

    fun hentTransaksjonerPåBidragssak(saksnummerRequest: SaksnummerRequest): TransaksjonerDto {
        val respons = reskWSSoapProxy.rohTransPrSak(
            3,
            saksnummerRequest.saksnummer.verdi.toInt(),
            Calendar.getInstance().apply { set(1900, 1, 1) },
            Calendar.getInstance().apply { set(9999, 1, 1) },
            Int.MAX_VALUE,
        )
        validerRespons(respons, "rohTransPrSak")
        TODO("Not yet implemented")
    }

    fun hentTransaksjonerPåPerson(personRequest: PersonRequest): TransaksjonerDto {
        val respons = reskWSSoapProxy.rohTransPrPersPrOrg(
            4,
            personRequest.ident.verdi,
            Calendar.getInstance().apply { set(1900, 1, 1) },
            Calendar.getInstance().apply { set(9999, 1, 1) },
            Int.MAX_VALUE,
        )
        validerRespons(respons, "rohTransPrPersPrOrg")
        TODO("Not yet implemented")
    }

    fun hentTransaksjonerPåTransaksjonsid(transaksjonsid: Long): TransaksjonerDto {
        val respons = reskWSSoapProxy.rohTransPrTransID(
            5,
            transaksjonsid.toString(),
            Calendar.getInstance().apply { set(1900, 1, 1) },
            Calendar.getInstance().apply { set(9999, 1, 1) },
        )
        validerRespons(respons, "rohTransPrTransID")
        TODO("Not yet implemented")
    }

    fun hentInformasjonOmInnkrevingssaken(personRequest: PersonRequest): InnkrevingssaksinformasjonDto {
        val respons = reskWSSoapProxy.rohInnkrevInfo(
            6,
            personRequest.ident.verdi,
        )
        validerRespons(respons, "rohInnkrevInfo")
        TODO("Not yet implemented")
    }

    fun endreRmForSak(saksnummer: Saksnummer, barn: Personident, nyttFødselsnummer: Personident) {
        val now = LocalDate.now()
        val respons = reskWSSoapProxy.rohRMEndreFNR(
            8,
            "RM",
            saksnummer.verdi.toInt(),
            "22222222226",
            barn.verdi,
            nyttFødselsnummer.verdi,
            Calendar.getInstance().apply { set(now.year, now.monthValue, now.dayOfMonth) },
        )
        validerEndreRmRespons(respons, saksnummer)
        TODO("Not yet implemented")
    }

    private fun validerRespons(respons: CResknObjectHolder?, navnPåEndepunkt: String) {
        if (respons == null || respons._Retur == null) {
            error("Ingen retur fra Elin.$navnPåEndepunkt!")
        }
        if (harReturkodeFeil(respons._Retur)) {
            error("Noe gikk galt i kallet mot $navnPåEndepunkt i Elin: ${respons._Retur._Sbeskr}")
        }
    }

    private fun validerEndreRmRespons(respons: CResknObjectHolder?, saksnummer: Saksnummer) {
        if (respons == null || respons._Retur == null) {
            error("Ingen retur fra Elin.rohRMEndreFNR for sak ${saksnummer.verdi}!")
        }
        if (harEndreRmFeil(respons._Retur)) {
            error("Noe gikk galt i kallet rohRMEndreFNR i Elin: ${respons._Retur._Sbeskr} for sak ${saksnummer.verdi}")
        }
    }

    fun harReturkodeFeil(returkode: Cretur): Boolean {
        return when (parseReturkode(returkode._Skode)) {
            -1, -2 -> true
            else -> false
        }
    }

    fun harEndreRmFeil(retCode: Cretur): Boolean {
        return parseReturkode(retCode._Skode) != 0
    }

    private fun parseReturkode(kode: String): Int {
        if (kode.isBlank()) {
            error("Returkode var blank!")
        }
        return kode.toInt()
    }
}
