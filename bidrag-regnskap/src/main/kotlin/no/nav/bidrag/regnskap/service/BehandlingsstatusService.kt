package no.nav.bidrag.regnskap.service

import no.nav.bidrag.domene.enums.regnskap.behandlingsstatus.Batchstatus
import no.nav.bidrag.regnskap.consumer.SkattConsumer
import no.nav.bidrag.regnskap.persistence.entity.Kontering
import no.nav.bidrag.transport.regnskap.behandlingsstatus.BehandlingsstatusResponse
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BehandlingsstatusService(
    private val skattConsumer: SkattConsumer,
    private val persistenceService: PersistenceService,
) {

    fun hentKonteringerMedIkkeGodkjentBehandlingsstatus(sisteReferanser: List<String> = emptyList()): Map<String, Set<Kontering>> = hentKonteringer(sisteReferanser)
        .groupBy { it.sisteReferansekode!! }
        .mapValues { it.value.toSet() }

    fun hentBehandlingsstatusForIkkeGodkjenteKonteringer(
        konteringerSomIkkeHarFåttGodkjentBehandlingsstatus: Map<String, Set<Kontering>>,
    ): Map<String, String> {
        val feilmeldinger = hashMapOf<String, String>()
        konteringerSomIkkeHarFåttGodkjentBehandlingsstatus.forEach { (batchUid, konteringer) ->
            try {
                val behandlingsstatusResponse = hentBehandlingsstatus(batchUid)

                if (behandlingsstatusResponse.batchStatus == Batchstatus.Done) {
                    behandleVellykkedeKonteringer(konteringer)
                } else {
                    behandleFeiledeKonteringer(
                        feilmeldinger,
                        batchUid,
                        konteringer,
                        "Behandling av konteringer for batchuid $batchUid har feilet: $behandlingsstatusResponse\n",
                    )
                }
            } catch (e: Exception) {
                behandleFeiledeKonteringer(
                    feilmeldinger,
                    batchUid,
                    konteringer,
                    "Behandling av konteringer for batchuid $batchUid har feilet og kastet exception!: ${e.message}\n",
                )
            }
        }
        return feilmeldinger
    }

    private fun behandleFeiledeKonteringer(
        feilmeldinger: HashMap<String, String>,
        batchUid: String,
        konteringer: Set<Kontering>,
        feilmelding: String,
    ) {
        feilmeldinger[batchUid] = feilmelding
        settFeiledeKonteringerForAlleOppdragKnyttetTilKonteringer(konteringer, true)
    }

    private fun behandleVellykkedeKonteringer(
        konteringer: Set<Kontering>,
    ) {
        val now = LocalDateTime.now()
        konteringer.forEach {
            it.behandlingsstatusOkTidspunkt = now
        }

        // Om alle konteringer knyttet til alle oppdragsperiodene i oppdraget er oversendt ok
        // så kan vi garantere at det ikke finnes feilede konteringer
        konteringer.firstOrNull()?.let { kontering ->
            if (erAlleKonteringerIOppdragOk(kontering)) {
                settFeiledeKonteringerForAlleOppdragKnyttetTilKonteringer(konteringer, false)
            }
        }
    }

    private fun settFeiledeKonteringerForAlleOppdragKnyttetTilKonteringer(konteringer: Set<Kontering>, harFeiledeKonteringer: Boolean) {
        konteringer.forEach { kontering -> kontering.oppdragsperiode!!.oppdrag!!.harFeiledeKonteringer = harFeiledeKonteringer }
    }

    private fun erAlleKonteringerIOppdragOk(kontering: Kontering): Boolean {
        val oppdragsperioder = kontering.oppdragsperiode!!.oppdrag!!.oppdragsperioder
        return oppdragsperioder
            .flatMap { it.konteringer }
            .none { it.behandlingsstatusOkTidspunkt == null }
    }

    fun hentBehandlingsstatusForIkkeGodkjenteKonteringerForReferansekode(sisteReferanser: List<String>): Map<String, String> = hentBehandlingsstatusForIkkeGodkjenteKonteringer(hentKonteringerMedIkkeGodkjentBehandlingsstatus(sisteReferanser))

    private fun hentKonteringer(sisteReferanser: List<String>): List<Kontering> = if (sisteReferanser.isEmpty()) {
        persistenceService.hentAlleKonteringerUtenBehandlingsstatusOk()
    } else {
        persistenceService.hentKonteringerUtenBehandlingsstatusOkForReferansekode(sisteReferanser)
    }

    private fun hentBehandlingsstatus(batchUid: String): BehandlingsstatusResponse {
        val behandlingsstatus = skattConsumer.sjekkBehandlingsstatus(batchUid).body
        return behandlingsstatus
            ?: error("Sjekk av behandlingsstatus feilet for batchUid: $batchUid!")
    }
}
