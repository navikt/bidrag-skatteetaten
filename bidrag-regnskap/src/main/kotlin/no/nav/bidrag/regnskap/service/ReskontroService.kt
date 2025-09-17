package no.nav.bidrag.regnskap.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.regnskap.consumer.BidragReskontroConsumer
import no.nav.bidrag.regnskap.persistence.entity.Kontering
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import org.springframework.stereotype.Service

private val LOGGER = KotlinLogging.logger { }

@Service
class ReskontroService(
    private val bidragReskontroConsumer: BidragReskontroConsumer,
    private val behandlingsstatusService: BehandlingsstatusService,
) {

    /**
     * Denne metoden sammenligner konteringer med hva som finnes i reskontro. Den kan ta input med konteringer, eller så henter den alle oversendte konteringer som ikke har fått godkjent behandlingsstatus.
     * Metoden tar deretter å oppretter en liste med feilmelding om det finnes avvik mellom oversending og reskontro.
     * Dette er ikke ment som en erstatning av sjekkAvBehandlingstatus, men heller som et suplement.
     */
    fun sammenlignOversendteKonteringerMedReskontro(inputKonteringer: Map<String, Set<Kontering>> = emptyMap()): HashMap<String, String> {
        val oversendteKonteringer = inputKonteringer.ifEmpty { behandlingsstatusService.hentKonteringerMedIkkeGodkjentBehandlingsstatus() }

        val feilmeldinger: HashMap<String, String> = hashMapOf()
        oversendteKonteringer.forEach { (sisteReferansekode, konteringer) ->
            // Siden alle konteringer oversendt for en referansekode alltid er knyttet til samme sak og vedtak kan vi hente saksnummer fra første kontering
            val saksnummer = konteringer.first().oppdragsperiode!!.oppdrag!!.sakId

            val transaksjoner = bidragReskontroConsumer.hentTransasksjonerForSak(saksnummer)
            if (transaksjoner == null || transaksjoner.transaksjoner.isEmpty()) {
                LOGGER.warn { "Fant ingen transaksjoner i reskontro for sak: $saksnummer for konteringer: $konteringer" }
                feilmeldinger[sisteReferansekode] = "Det finnes ingen transaksjoner i reskontro for sak: $saksnummer, vedtak: ${konteringer.first().vedtakId}.\n"
                return@forEach
            }

            konteringer.forEach { kontering ->
                val transaksjonSomMatcherKontering = finnMatcheneTransaksjon(transaksjoner, kontering)
                if (transaksjonSomMatcherKontering == null) {
                    feilmeldinger.putIfAbsent(sisteReferansekode, "") // Hvis det ikke finnes en feilmelding for referansekode må dette gjøres for å unngå en null-referanse når stringen appendes under.
                    feilmeldinger[sisteReferansekode] += "Fant ikke transaksjon i reskontro for sak: $saksnummer, vedtak: ${kontering.vedtakId}, transaksjonskode: ${kontering.transaksjonskode}, periode: ${kontering.overføringsperiode} som matcher oversendt kontering: ${kontering.konteringId}.\n"
                }
            }
        }
        return feilmeldinger
    }

    private fun finnMatcheneTransaksjon(
        transaksjoner: TransaksjonerDto,
        kontering: Kontering,
    ): TransaksjonDto? = transaksjoner.transaksjoner.find { transaksjon ->
        val sammeDelytelsesId = transaksjon.delytelsesid == kontering.oppdragsperiode!!.delytelseId.toString()
        val sammeTransaksjonskode = transaksjon.transaksjonskode == kontering.transaksjonskode
        val sammePeriode = transaksjon.periode?.toMånedsperiode()?.fom.toString() == kontering.overføringsperiode
        sammeDelytelsesId &&
            sammeTransaksjonskode &&
            sammePeriode
    }
}
