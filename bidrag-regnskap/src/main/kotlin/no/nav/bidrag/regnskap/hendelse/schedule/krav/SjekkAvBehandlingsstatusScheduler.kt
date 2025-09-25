package no.nav.bidrag.regnskap.hendelse.schedule.krav

import io.github.oshai.kotlinlogging.KotlinLogging
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.bidrag.regnskap.persistence.entity.Kontering
import no.nav.bidrag.regnskap.service.BehandlingsstatusService
import no.nav.bidrag.regnskap.service.ReskontroService
import no.nav.bidrag.regnskap.slack.SlackService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

private val LOGGER = KotlinLogging.logger { }

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
class SjekkAvBehandlingsstatusScheduler(
    private val behandlingsstatusService: BehandlingsstatusService,
    private val kravSchedulerUtils: KravSchedulerUtils,
    private val slackService: SlackService,
    private val reskontroService: ReskontroService,
    @param:Value($$"${NAIS_CLIENT_ID}") private val clientId: String,
) {

    @Scheduled(cron = $$"${scheduler.behandlingsstatus.cron}")
    @SchedulerLock(name = "skedulertSjekkAvBehandlingsstatus")
    @Transactional
    fun skedulertSjekkAvBehandlingsstatus() {
        LockAssert.assertLocked()
        LOGGER.info { "Starter schedulert sjekk av behandlingsstatus for allerede overførte konteringer." }
        if (kravSchedulerUtils.harAktivtDriftsavvik()) {
            LOGGER.warn { "Det finnes aktive driftsavvik. Starter derfor ikke sjekk av behandlingsstatus." }
            return
        } else if (kravSchedulerUtils.erVedlikeholdsmodusPåslått()) {
            LOGGER.warn { "Vedlikeholdsmodus er påslått! Starter derfor ikke sjekk av behandlingsstatus." }
            return
        }

        val ikkeGodkjenteKonteringer = behandlingsstatusService.hentKonteringerMedIkkeGodkjentBehandlingsstatus()

        if (ikkeGodkjenteKonteringer.isEmpty()) {
            LOGGER.info { "Det finnes ingen konteringer som ikke har sjekket behandlingsstatus." }
            return
        }

        val feiledeOverføringer: Map<String, String> =
            behandlingsstatusService.hentBehandlingsstatusForIkkeGodkjenteKonteringer(ikkeGodkjenteKonteringer)

        loggBatchUiderSomFremdelesFeiler(ikkeGodkjenteKonteringer)

        if (feiledeOverføringer.isNotEmpty()) {
            val feilmeldingerReskontro =
                reskontroService.sammenlignOversendteKonteringerMedReskontro(ikkeGodkjenteKonteringer)

            var feilmeldingSammenslått = ""

            feiledeOverføringer.forEach { (batchUid, feilmelding) ->

                if (!feilmeldingerReskontro.containsKey(batchUid) && !erForskudd(ikkeGodkjenteKonteringer)) {
                    LOGGER.warn { "BatchUId $batchUid har alle konteringer registert i reskontro. Makerer derfor batchUid som OK. Denne batchUid burde ha returnert DONE fra Skatt." }
                    behandlingsstatusService.behandleVellykkedeKonteringer(ikkeGodkjenteKonteringer[batchUid]!!)
                } else {
                    val nyFeilmelding = utledFeilmelding(ikkeGodkjenteKonteringer, feilmelding, feilmeldingerReskontro, batchUid)
                    feilmeldingSammenslått += nyFeilmelding
                }
            }

            if (feilmeldingSammenslått.isEmpty()) {
                return
            }

            if (erInnenforDagligSlackTidsvindu()) {
                slackService.sendMelding(
                    ":ohno: Sjekk av behandlingsstatus feilet! Miljø: $clientId\n\nFølgende batchUider feilet:\n$feilmeldingSammenslått",
                )
            }
            LOGGER.error { "Det har oppstått feil ved overføring av krav på følgende batchUider med følgende feilmelding:\n $feilmeldingSammenslått" }
        }
    }

    private fun loggBatchUiderSomFremdelesFeiler(ikkeGodkjenteKonteringer: Map<String, Set<Kontering>>) {
        LOGGER.info {
            "${ikkeGodkjenteKonteringer.size} batchUider har fremdeles feil behandlingsstatus. (${
                ikkeGodkjenteKonteringer.entries.joinToString(
                    ", ",
                ) { it.key }
            })"
        }
    }

    private fun utledFeilmelding(
        konteringerSomIkkeHarFåttGodkjentBehandlingsstatus: Map<String, Set<Kontering>>,
        feilmelding: String,
        feilmeldingerReskontro: HashMap<String, MutableSet<String>>,
        batchUid: String,
    ): String {
        if (!erForskudd(konteringerSomIkkeHarFåttGodkjentBehandlingsstatus)) {
            return "$feilmelding\nDenne batchUiden har ${feilmeldingerReskontro[batchUid]?.size} feilede av totalt ${konteringerSomIkkeHarFåttGodkjentBehandlingsstatus[batchUid]?.size} konteringer.\n\n"
        }
        return "$feilmelding\nDenne batchUiden inneholder forskudd som ikke nødvendigvis finnes i reskontro enda.\n\n"
    }

    private fun erForskudd(konteringerSomIkkeHarFåttGodkjentBehandlingsstatus: Map<String, Set<Kontering>>): Boolean = konteringerSomIkkeHarFåttGodkjentBehandlingsstatus.any { (_, konteringer) -> konteringer.any { it.transaksjonskode == "A1" || it.transaksjonskode == "A3" } }

    // Sørger for at slack melding på behandlingsstatus kun blir sendt en gang om dagen
    private fun erInnenforDagligSlackTidsvindu(): Boolean {
        val now = LocalTime.now()
        val tidFra = LocalTime.parse("07:00:00")
        val tidTil = LocalTime.parse("08:00:00")
        return tidFra.isBefore(now) && tidTil.isAfter(now)
    }
}
