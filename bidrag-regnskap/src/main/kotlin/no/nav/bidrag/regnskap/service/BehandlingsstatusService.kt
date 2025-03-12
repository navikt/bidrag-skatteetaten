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

    fun hentKonteringerMedIkkeGodkjentBehandlingsstatus(sisteReferanser: List<String> = emptyList()): HashMap<String, MutableSet<Kontering>> {
        val map = HashMap<String, MutableSet<Kontering>>()
        hentKonteringer(sisteReferanser).forEach {
            map.getOrPut(it.sisteReferansekode!!) { mutableSetOf() }.add(it)
        }
        return map
    }

    fun hentBehandlingsstatusForIkkeGodkjenteKonteringer(
        konteringerSomIkkeHarFåttGodkjentBehandlingsstatus: HashMap<String, MutableSet<Kontering>>,
    ): HashMap<String, String> {
        val feilmeldinger = hashMapOf<String, String>()
        konteringerSomIkkeHarFåttGodkjentBehandlingsstatus.forEach { (batchUid, konteringer) ->
            try {
                val behandlingsstatusResponse = hentBehandlingsstatus(batchUid)
                val now = LocalDateTime.now()
                if (behandlingsstatusResponse.batchStatus == Batchstatus.Done) {
                    konteringer.forEach {
                        it.behandlingsstatusOkTidspunkt = now
                    }

                    // Om alle konteringer knyttet til alle oppdragsperiodene i oppdraget er oversendt ok
                    // så kan vi garantere at det ikke finnes feilede konteringer
                    konteringer.firstOrNull()?.let { kontering ->
                        if (kontering.oppdragsperiode!!.oppdrag!!.oppdragsperioder.flatMap { it.konteringer }
                                .none { it.behandlingsstatusOkTidspunkt == null }
                        ) {
                            kontering.oppdragsperiode.oppdrag!!.harFeiledeKonteringer = false
                        }
                    }
                } else {
                    feilmeldinger[batchUid] = "Behandling av konteringer for batchuid $batchUid har feilet: $behandlingsstatusResponse\n"
                    konteringer.firstOrNull()?.let {
                        it.oppdragsperiode!!.oppdrag!!.harFeiledeKonteringer = true
                    }
                }
            } catch (e: Exception) {
                feilmeldinger[batchUid] = "Behandling av konteringer for batchuid $batchUid har feilet og kastet exception!: ${e.message}\n"
                konteringer.firstOrNull()?.let {
                    it.oppdragsperiode!!.oppdrag!!.harFeiledeKonteringer = true
                }
            }
        }
        return feilmeldinger
    }

    fun hentBehandlingsstatusForIkkeGodkjenteKonteringerForReferansekode(sisteReferanser: List<String>): HashMap<String, String> = hentBehandlingsstatusForIkkeGodkjenteKonteringer(hentKonteringerMedIkkeGodkjentBehandlingsstatus(sisteReferanser))

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
