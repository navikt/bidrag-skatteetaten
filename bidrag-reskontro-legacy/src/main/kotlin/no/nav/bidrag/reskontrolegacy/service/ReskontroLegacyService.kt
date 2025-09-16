package no.nav.bidrag.reskontrolegacy.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.domene.ident.Organisasjonsnummer
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.Datoperiode
import no.nav.bidrag.reskontrolegacy.generated.CResknObjectHolder
import no.nav.bidrag.reskontrolegacy.generated.Cretur
import no.nav.bidrag.reskontrolegacy.reskws.ReskWsClient
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakMedSkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.SaksinformasjonBarnDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.SkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.GjeldendeBetalingsordningDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssakshistorikkDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssaksinformasjonDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.NyBetalingsordningDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.SkyldnerinformasjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private val LOGGER = KotlinLogging.logger {}

@Service
class ReskontroLegacyService(
    private val reskWSSoapProxy: ReskWsClient,
) {

    fun hentInnkrevingssakPåSak(saksnummerRequest: SaksnummerRequest): BidragssakDto? {
        val respons = reskWSSoapProxy.rohPrSakPrBarn(
            saksnummerRequest.saksnummer.verdi.toInt(),
        )
        validerRespons(respons, "rohPrSakPrBarn")

        val bidragSak = respons?.utParameter?.colCbidragSak?.cbidragSak?.getOrNull(0)

        if (bidragSak == null) {
            LOGGER.info { "Fant ingen bidragssak på hentInnkrevingssakPåSak: ${saksnummerRequest.saksnummer.verdi} for rohPrSakPrBarn" }
            return null
        }

        return BidragssakDto(
            saksnummer = Saksnummer(bidragSak.nBidragsSaksnr.toString()),
            bmGjeldFastsettelsesgebyr = bidragSak.dbmGjeldFastsGebyr,
            bpGjeldFastsettelsesgebyr = bidragSak.dbpGjeldFastGebyr,
            bmGjeldRest = bidragSak.dbmGjeldRest,
            barn = bidragSak.colCbarnISak.cbarnISak.map {
                SaksinformasjonBarnDto(
                    personident = Personident(it.sFnr),
                    restGjeldOffentlig = it.dbpBelGjeldOff,
                    restGjeldPrivat = it.dbpBelGjeldPrivTot,
                    // TODO(Må sjekke opp om det under er riktig felt for denne verdien)
                    sumIkkeUtbetalt = it.dbidUtbetTot,
                    sumForskuddUtbetalt = it.dforskUtbetTot,
                    periode = Datoperiode(
                        LocalDateTime.ofInstant(
                            it.dtbidPerSisteDatoFom.toGregorianCalendar().toInstant(),
                            it.dtbidPerSisteDatoFom.toGregorianCalendar().getTimeZone().toZoneId(),
                        ).toLocalDate(),
                        LocalDateTime.ofInstant(
                            it.dtbidPerSisteDatoTom.toGregorianCalendar().toInstant(),
                            it.dtbidPerSisteDatoTom.toGregorianCalendar().getTimeZone().toZoneId(),
                        ).toLocalDate(),
                    ),
                    erStoppIUtbetaling = "J" == it.sstoppFord,
                )
            },
        )
    }

    fun hentInnkrevingssakPåPerson(personRequest: PersonRequest): BidragssakMedSkyldnerDto? {
        val respons = reskWSSoapProxy.rohPrPersPrSakPrBarn(
            personRequest.ident.verdi,
        )
        validerRespons(respons, "rohPrPersPrSakPrBarn")
        val bidragSak = respons?.utParameter?.colCbidragSak?.cbidragSak
        val skyldner = respons?.utParameter?.colCpersOrg?.cpersOrg?.getOrNull(0)

        return BidragssakMedSkyldnerDto(
            skyldner = skyldner?.let {
                SkyldnerDto(
                    personident = Personident(skyldner.sfnr),
                    innbetaltBeløpUfordelt = skyldner.dinnbetBelopUford,
                    gjeldIlagtGebyr = skyldner.dgjeldGebyrIlagtTI,
                )
            },
            bidragssaker = bidragSak?.map { it ->
                BidragssakDto(
                    saksnummer = Saksnummer(it.nBidragsSaksnr.toString()),
                    bmGjeldFastsettelsesgebyr = it.dbmGjeldFastsGebyr,
                    bpGjeldFastsettelsesgebyr = it.dbpGjeldFastGebyr,
                    bmGjeldRest = it.dbmGjeldRest,
                    barn = it.colCbarnISak.cbarnISak.map {
                        SaksinformasjonBarnDto(
                            personident = Personident(it.sFnr),
                            restGjeldOffentlig = it.dbpBelGjeldOff,
                            restGjeldPrivat = it.dbpBelGjeldPrivTot,
                            sumForskuddUtbetalt = it.dforskUtbetTot,
                            restGjeldPrivatAndel = it.dbpBelGjeldPrivAndel,
                            sumUtbetaltAndel = it.dbidUtbetAndel,
                            sumForskuddUtbetaltAndel = it.dforskUtbetAndel,
                        )
                    },
                )
            },
        )
    }

    fun hentTransaksjonerPåBidragssak(saksnummerRequest: SaksnummerRequest): TransaksjonerDto? {
        val respons = reskWSSoapProxy.rohTransPrSak(
            saksnummerRequest.saksnummer.verdi.toInt(),
        )
        validerRespons(respons, "rohTransPrSak")

        return opprettTransaksjonerRespons(respons, saksnummerRequest.saksnummer.verdi, "hentTransaksjonerPåBidragssak", "rohTransPrSak")
    }

    fun hentTransaksjonerPåPerson(personRequest: PersonRequest): TransaksjonerDto? {
        val respons = reskWSSoapProxy.rohTransPrPersPrOrg(
            personRequest.ident.verdi,
        )
        validerRespons(respons, "rohTransPrPersPrOrg")

        return opprettTransaksjonerRespons(respons, personRequest.ident.verdi, "hentTransaksjonerPåPerson", "rohTransPrPersPrOrg")
    }

    fun hentTransaksjonerPåTransaksjonsid(transaksjonsid: Long): TransaksjonerDto? {
        val respons = reskWSSoapProxy.rohTransPrTransID(
            transaksjonsid.toString(),
        )
        validerRespons(respons, "rohTransPrTransID")

        return opprettTransaksjonerRespons(respons, transaksjonsid.toString(), "hentTransaksjonerPåTransaksjonsid", "rohTransPrTransID")
    }

    fun hentInformasjonOmInnkrevingssaken(personRequest: PersonRequest): InnkrevingssaksinformasjonDto? {
        val respons = reskWSSoapProxy.rohInnkrevInfo(
            personRequest.ident.verdi,
        )
        validerRespons(respons, "rohInnkrevInfo")

        val skyldner = respons?.utParameter?.colCpersOrg?.cpersOrg?.getOrNull(0)
        val gjeldendeBetalingsordning = respons?.utParameter?.colCgjeldBetOrdn?.cgjeldBetOrdn?.getOrNull(0)
        val nyUtbetalingsordning = respons?.utParameter?.colCnyBetOrdn?.cnyBetOrdn?.getOrNull(0)

        return InnkrevingssaksinformasjonDto(
            skyldnerinformasjon = skyldner?.let {
                SkyldnerinformasjonDto(
                    personident = Personident(skyldner.sfnr),
                    sumLøpendeBidrag = skyldner.dtotLopBidrag,
                    innkrevingssaksstatus = skyldner.statusInnkrevBeskr,
                    fakturamåte = skyldner.fakturaMaateBeskr,
                    sisteAktivitet = skyldner.sisteAktivitetBeskr,
                )
            },
            gjeldendeBetalingsordning = gjeldendeBetalingsordning?.let {
                GjeldendeBetalingsordningDto(
                    typeBehandlingsordning = gjeldendeBetalingsordning.stypeBetOrdBesk,
                    kilde = Organisasjonsnummer(gjeldendeBetalingsordning.sbetKildeFOnr),
                    kildeNavn = gjeldendeBetalingsordning.sbetKildeNavn,
                    datoSisteGiro = LocalDateTime.ofInstant(
                        gjeldendeBetalingsordning.dtDatoSisteGiro.toGregorianCalendar().toInstant(),
                        gjeldendeBetalingsordning.dtDatoSisteGiro.toGregorianCalendar().getTimeZone().toZoneId(),
                    ),
                    nesteForfall = LocalDateTime.ofInstant(
                        gjeldendeBetalingsordning.dtDatoNesteForfall.toGregorianCalendar().toInstant(),
                        gjeldendeBetalingsordning.dtDatoNesteForfall.toGregorianCalendar().getTimeZone().toZoneId(),
                    ),
                    beløp = gjeldendeBetalingsordning.dBelop,
                    sistEndret = LocalDateTime.ofInstant(
                        gjeldendeBetalingsordning.dtDatoSistEndret.toGregorianCalendar().toInstant(),
                        gjeldendeBetalingsordning.dtDatoSistEndret.toGregorianCalendar().getTimeZone().toZoneId(),
                    ),
                    sistEndretÅrsak = gjeldendeBetalingsordning.sAarsakSistEndret,
                    sumUbetalt = gjeldendeBetalingsordning.dSumUbetOrdning,
                )
            },
            nyBetalingsordning = nyUtbetalingsordning?.let {
                NyBetalingsordningDto(
                    dato = Datoperiode(
                        LocalDateTime.ofInstant(
                            nyUtbetalingsordning.dtDatoFOM.toGregorianCalendar().toInstant(),
                            nyUtbetalingsordning.dtDatoFOM.toGregorianCalendar().getTimeZone().toZoneId(),
                        ).toLocalDate(),
                        null,
                    ),
                    beløp = nyUtbetalingsordning.dBelop,
                )
            },
            innkrevingssakshistorikk = respons?.utParameter?.colCinnkrevAktivHist?.cinnkrevAktivHist?.map {
                InnkrevingssakshistorikkDto(
                    beskrivelse = it.shendBeskr,
                    ident = Ident(it.sfOnr),
                    navn = it.sNavn,
                    dato = LocalDateTime.ofInstant(
                        it.dtDatoFOM.toGregorianCalendar().toInstant(),
                        it.dtDatoFOM.toGregorianCalendar().getTimeZone().toZoneId(),
                    ),
                    beløp = it.dBelopHendelse,
                )
            },
        )
    }

    fun endreRmForSak(saksnummer: Saksnummer, barn: Personident, nyttFødselsnummer: Personident) {
        val respons = reskWSSoapProxy.rohRMEndreFNR(
            saksnummer.verdi.toInt(),
            barn.verdi,
            nyttFødselsnummer.verdi,
        )
        validerEndreRmRespons(respons, saksnummer)
    }

    private fun opprettTransaksjonerRespons(
        respons: CResknObjectHolder?,
        søkeparameter: String,
        metodenavn: String,
        elinKallNavn: String,
    ): TransaksjonerDto? {
        val transaksjoner = respons?.utParameter?.colCtransaksjon?.ctransaksjon

        if (transaksjoner.isNullOrEmpty()) {
            LOGGER.info { "Fant ingen transaksjoner på $metodenavn: $søkeparameter for $elinKallNavn" }
            return null
        }

        return TransaksjonerDto(
            transaksjoner = transaksjoner.map {
                TransaksjonDto(
                    transaksjonsid = it.stransID.toLong(),
                    transaksjonskode = it.stransKode,
                    beskrivelse = it.stransBeskr,
                    dato = LocalDateTime.ofInstant(
                        it.dtpostDato.toGregorianCalendar().toInstant(),
                        it.dtpostDato.toGregorianCalendar().getTimeZone().toZoneId(),
                    ).toLocalDate(),
                    skyldner = Personident(it.stransKildeFOnr),
                    mottaker = Personident(it.stransMottakerFOnr),
                    beløp = it.dopprBelop,
                    restBeløp = it.drestBelop,
                    beløpIOpprinneligValuta = it.dvalutaOpprBelop,
                    valutakode = it.svalutaKode,
                    saksnummer = Saksnummer(it.nbidragSaksnr.toString()),
                    periode = Datoperiode(
                        LocalDateTime.ofInstant(
                            it.dtbidPerDatoFom.toGregorianCalendar().toInstant(),
                            it.dtbidPerDatoFom.toGregorianCalendar().getTimeZone().toZoneId(),
                        ).toLocalDate(),
                        LocalDateTime.ofInstant(
                            it.dtbidPerDatoTom.toGregorianCalendar().toInstant(),
                            it.dtbidPerDatoTom.toGregorianCalendar().getTimeZone().toZoneId(),
                        ).toLocalDate().plusDays(1),
                    ),
                    barn = Personident(it.sbarnFnr),
                    delytelsesid = it.sbidragId,
                    søknadstype = it.sSoknType,
                )
            },
        )
    }

    private fun validerRespons(respons: CResknObjectHolder?, navnPåEndepunkt: String) {
        if (respons == null || respons.retur == null || respons.utParameter == null || respons.innParameter == null) {
            error("Ingen retur fra Elin.$navnPåEndepunkt!")
        }
        if (harReturkodeFeil(respons.retur)) {
            error("Noe gikk galt i kallet mot $navnPåEndepunkt i Elin: ${respons.retur.sbeskr}")
        }
    }

    private fun validerEndreRmRespons(respons: CResknObjectHolder?, saksnummer: Saksnummer) {
        if (respons == null || respons.retur == null) {
            error("Ingen retur fra Elin.rohRMEndreFNR for sak ${saksnummer.verdi}!")
        }
        if (harEndreRmFeil(respons.retur)) {
            error("Noe gikk galt i kallet rohRMEndreFNR i Elin: ${respons.retur.sbeskr} for sak ${saksnummer.verdi}")
        }
    }

    fun harReturkodeFeil(returkode: Cretur): Boolean = when (parseReturkode(returkode.skode)) {
        -1, -2 -> true
        else -> false
    }

    fun harEndreRmFeil(retCode: Cretur): Boolean = parseReturkode(retCode.skode) != 0

    private fun parseReturkode(kode: String): Int {
        if (kode.isBlank()) {
            error("Returkode var blank!")
        }
        return kode.toInt()
    }
}
