package no.nav.bidrag.reskontro.service

import no.nav.bidrag.commons.security.maskinporten.MaskinportenClientException
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.domene.ident.Organisasjonsnummer
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.Datoperiode
import no.nav.bidrag.reskontro.consumer.SkattReskontroConsumer
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerOutput
import no.nav.bidrag.reskontro.exceptions.IngenDataFraSkattException
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReskontroService(private val skattReskontroConsumer: SkattReskontroConsumer) {

    fun hentInnkrevingssakPåSak(saksnummerRequest: SaksnummerRequest): BidragssakDto? {
        val innkrevingssakResponse = skattReskontroConsumer.hentInnkrevningssakerPåSak(saksnummerRequest.saksnummer.verdi.toLong())
        val innkrevingssak = validerOutput(innkrevingssakResponse)

        val sak = innkrevingssak.bidragssaker?.first { it.bidragssaksnummer.toString() == saksnummerRequest.saksnummer.verdi }

        return BidragssakDto(
            saksnummer = Saksnummer(sak?.bidragssaksnummer.toString()),
            bmGjeldFastsettelsesgebyr = sak?.bmGjeldFastsettelsesgebyr,
            bpGjeldFastsettelsesgebyr = sak?.bpGjeldFastsettelsesgebyr,
            bmGjeldRest = sak?.bmGjeldRest,
            barn =
            sak?.perBarnISak?.map { it ->
                SaksinformasjonBarnDto(
                    personident = it.fodselsnummer?.let { Personident(it) },
                    restGjeldOffentlig = it.restGjeldOffentlig,
                    restGjeldPrivat = it.restGjeldPrivat,
                    sumIkkeUtbetalt = it.sumIkkeUtbetalt,
                    sumForskuddUtbetalt = it.sumForskuddUtbetalt,
                    periode =
                    Datoperiode(
                        LocalDateTime.parse(it.periodeSisteDatoFom!!).toLocalDate(),
                        it.periodeSisteDatoTom?.let { tom -> LocalDateTime.parse(tom).toLocalDate().plusDays(1) },
                    ),
                    erStoppIUtbetaling = it.stoppUtbetaling!! == "J",
                )
            } ?: emptyList(),
        )
    }

    fun hentInnkrevingssakPåPerson(personRequest: PersonRequest): BidragssakMedSkyldnerDto? {
        val innkrevingssakResponse = skattReskontroConsumer.hentInnkrevningssakerPåPerson(personRequest.ident)
        val innkrevingssak = validerOutput(innkrevingssakResponse)

        return BidragssakMedSkyldnerDto(
            skyldner =
            SkyldnerDto(
                personident = innkrevingssak.skyldner?.fodselsOrgnr?.let { Personident(it) },
                innbetaltBeløpUfordelt = innkrevingssak.skyldner?.innbetBelopUfordelt,
                gjeldIlagtGebyr = innkrevingssak.skyldner?.gjeldIlagtGebyr,
            ),
            bidragssaker = innkrevingssak.bidragssaker?.map { it ->
                BidragssakDto(
                    saksnummer = Saksnummer(it.bidragssaksnummer.toString()),
                    bmGjeldFastsettelsesgebyr = it.bmGjeldFastsettelsesgebyr,
                    bpGjeldFastsettelsesgebyr = it.bpGjeldFastsettelsesgebyr,
                    bmGjeldRest = it.bmGjeldRest,
                    barn =
                    it.perBarnISak?.map {
                        SaksinformasjonBarnDto(
                            personident = it.fodselsnummer?.let { Personident(it) },
                            restGjeldOffentlig = it.restGjeldOffentlig,
                            restGjeldPrivat = it.restGjeldPrivat,
                            sumForskuddUtbetalt = it.sumForskuddUtbetalt,
                            restGjeldPrivatAndel = it.restGjeldPrivatAndel,
                            sumUtbetaltAndel = it.sumUtbetaltAndel,
                            sumForskuddUtbetaltAndel = it.sumForskuddUtbetaltAndel,
                        )
                    } ?: emptyList(),
                )
            },
        )
    }

    fun hentTransaksjonerPåBidragssak(saksnummerRequest: SaksnummerRequest): TransaksjonerDto? {
        val transaksjonerResponse =
            skattReskontroConsumer.hentTransaksjonerPåBidragssak(saksnummerRequest.saksnummer.verdi.toLong())
        val transaksjoner = validerOutput(transaksjonerResponse)
        return opprettTransaksjonerResponse(transaksjoner)
    }

    fun hentTransaksjonerPåPerson(personRequest: PersonRequest): TransaksjonerDto? {
        val transaksjonerResponse = skattReskontroConsumer.hentTransaksjonerPåPerson(personRequest.ident)
        val transaksjoner = validerOutput(transaksjonerResponse)
        return opprettTransaksjonerResponse(transaksjoner)
    }

    fun hentTransaksjonerPåTransaksjonsid(transaksjonsid: Long): TransaksjonerDto? {
        val transaksjonerResponse = skattReskontroConsumer.hentTransaksjonerPåTransaksjonsId(transaksjonsid)
        val transaksjoner = validerOutput(transaksjonerResponse)
        return opprettTransaksjonerResponse(transaksjoner)
    }

    fun hentInformasjonOmInnkrevingssaken(personRequest: PersonRequest): InnkrevingssaksinformasjonDto? {
        val innkrevingsinformasjonResponse = skattReskontroConsumer.hentInformasjonOmInnkrevingssaken(personRequest.ident)
        val innkrevingsinformasjon = validerOutput(innkrevingsinformasjonResponse)
        return InnkrevingssaksinformasjonDto(
            skyldnerinformasjon =
            SkyldnerinformasjonDto(
                personident = innkrevingsinformasjon.skyldner?.fodselsOrgnr?.let { Personident(it) },
                sumLøpendeBidrag = innkrevingsinformasjon.skyldner?.sumLopendeBidrag,
                innkrevingssaksstatus = innkrevingsinformasjon.skyldner?.statusInnkrevingssak,
                fakturamåte = innkrevingsinformasjon.skyldner?.fakturamaate,
                sisteAktivitet = innkrevingsinformasjon.skyldner?.sisteAktivitet,
            ),
            gjeldendeBetalingsordning =
            GjeldendeBetalingsordningDto(
                typeBehandlingsordning = innkrevingsinformasjon.gjeldendeBetalingsordning?.typeBetalingsordning,
                kilde = innkrevingsinformasjon.gjeldendeBetalingsordning?.kildeOrgnummer?.let { Organisasjonsnummer(it) },
                kildeNavn = innkrevingsinformasjon.gjeldendeBetalingsordning?.kildeNavn,
                datoSisteGiro = innkrevingsinformasjon.gjeldendeBetalingsordning?.datoSisteGiro?.let { LocalDateTime.parse(it) },
                nesteForfall = innkrevingsinformasjon.gjeldendeBetalingsordning?.datoNesteForfall?.let { LocalDateTime.parse(it) },
                beløp = innkrevingsinformasjon.gjeldendeBetalingsordning?.belop,
                sistEndret = innkrevingsinformasjon.gjeldendeBetalingsordning?.datoSistEndret?.let { LocalDateTime.parse(it) },
                sistEndretÅrsak = innkrevingsinformasjon.gjeldendeBetalingsordning?.aarsakSistEndret,
                sumUbetalt = innkrevingsinformasjon.gjeldendeBetalingsordning?.sumUbetalt,
            ),
            nyBetalingsordning =
            NyBetalingsordningDto(
                dato = innkrevingsinformasjon.nyBetalingsordning?.datoFraOgMed?.let { Datoperiode(LocalDateTime.parse(it).toLocalDate(), null) },
                beløp = innkrevingsinformasjon.nyBetalingsordning?.belop,
            ),
            innkrevingssakshistorikk =
            innkrevingsinformasjon.innkrevingssaksHistorikk!!.map { it ->
                InnkrevingssakshistorikkDto(
                    beskrivelse = it.beskrivelse,
                    ident = it.fodselsOrgNr?.let { Ident(it) },
                    navn = it.navn,
                    dato = it.dato?.let { LocalDateTime.parse(it) },
                    beløp = it.belop,
                )
            },
        )
    }

    fun endreRmForSak(saksnummer: Saksnummer, barn: Personident, nyRm: Personident) {
        val endreRmResponse = skattReskontroConsumer.endreRmForSak(saksnummer.verdi.toLong(), barn, nyRm)
        validerOutput(endreRmResponse)
    }

    private fun opprettTransaksjonerResponse(transaksjoner: ReskontroConsumerOutput) = TransaksjonerDto(
        transaksjoner =
        transaksjoner.transaksjoner!!.map { it ->
            TransaksjonDto(
                transaksjonsid = it.transaksjonsId,
                transaksjonskode = it.kode,
                beskrivelse = it.beskrivelse,
                dato = LocalDateTime.parse(it.dato!!).toLocalDate(),
                skyldner = it.kildeFodselsOrgNr?.let { Personident(it) },
                mottaker = it.mottakerFodselsOrgNr?.let { Personident(it) },
                beløp = it.opprinneligBeloep,
                restBeløp = it.restBeloep,
                beløpIOpprinneligValuta = it.valutaOpprinneligBeloep,
                valutakode = it.valutakode,
                saksnummer = Saksnummer(it.bidragssaksnummer.toString()),
                periode =
                Datoperiode(
                    LocalDateTime.parse(it.periodeSisteDatoFom!!).toLocalDate(),
                    it.periodeSisteDatoTom?.let { tom -> LocalDateTime.parse(tom).toLocalDate().plusDays(1) },
                ),
                barn = it.barnFodselsnr?.let { Personident(it) },
                delytelsesid = it.bidragsId,
                søknadstype = it.soeknadsType,
            )
        },
    )

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

        when (reskontroConsumerOutputResponse.body!!.retur!!.kode) {
            0 -> return reskontroConsumerOutputResponse.body!!
            -1 -> error("Kallet mot skatt feilet med feilmelding: ${reskontroConsumerOutputResponse.body!!.retur!!.beskrivelse}")
            -2 -> error("Kallet mot skatt hadde ugyldig aksjonskode! Dette er ikke basert på innput og må rettes i koden/hos skatt.")
            -3 -> throw IngenDataFraSkattException("Skatt svarte med ingen data.")
            else -> error("Kallet mot skatt returnerte ukjent returnkode ${reskontroConsumerOutputResponse.body!!.retur!!.kode}")
        }
    }
}
