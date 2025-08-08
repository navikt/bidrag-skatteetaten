package no.nav.bidrag.regnskap.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.regnskap.persistence.entity.Driftsavvik
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val LOGGER = KotlinLogging.logger { }

@Service
class DriftsavvikService(
    private val persistenceService: PersistenceService,
    private val kravService: KravService,
    private val oppdragsperiodeService: OppdragsperiodeService,
) {

    fun hentFlereDriftsavvik(antallDriftsavvik: Int): List<Driftsavvik> = persistenceService.hentFlereDriftsavvik(PageRequest.of(0, antallDriftsavvik))

    fun hentAlleAktiveDriftsavvik(): List<Driftsavvik> = persistenceService.hentAlleAktiveDriftsavvik()

    fun lagreDriftsavvik(tidspunktFra: LocalDateTime, tidspunktTil: LocalDateTime?, opprettetAv: String?, årsak: String?, skalStoppeInnlesning: Boolean?): Int = persistenceService.lagreDriftsavvik(
        Driftsavvik(tidspunktFra = tidspunktFra, tidspunktTil = tidspunktTil, opprettetAv = opprettetAv, årsak = årsak, skalStoppeInnlesning = skalStoppeInnlesning ?: true),
    )

    fun harAktivtDriftsavvik(erInnlesing: Boolean = false): Boolean = persistenceService.harAktivtDriftsavvik(erInnlesing)

    @Transactional
    fun endreDriftsavvik(driftsavvikId: Int, tidspunktTil: LocalDateTime?, skalStoppeInnlesning: Boolean?): Int? {
        val driftsavvik = persistenceService.hentDriftsavvik(driftsavvikId) ?: return null
        return persistenceService.lagreDriftsavvik(driftsavvik.copy(tidspunktTil = tidspunktTil, skalStoppeInnlesning = skalStoppeInnlesning ?: driftsavvik.skalStoppeInnlesning))
    }

    fun slippVedtakGjennomDriftsavvik(vedtakId: Int) {
        val oppdragsperioder = oppdragsperiodeService.hentAlleOppdragsperiodeMedVedtaksId(vedtakId)
        val oppdrag = oppdragsperioder.flatMap { listOf(it.oppdrag!!.oppdragId!!) }.distinct().toList()
        sendKrav(oppdrag)
    }

    fun sendKrav(oppdragIdListe: List<Int>) {
        if (erVedlikeholdsmodusPåslått()) {
            LOGGER.info { "Vedlikeholdsmodus er påslått! Starter derfor ikke overføring av kontering for oppdrag: $oppdragIdListe." }
            return
        }
        LOGGER.info { "Starter overføring av konteringer sluppet igjennom driftsavvik for oppdrag: $oppdragIdListe." }
        kravService.sendKrav(oppdragIdListe)
    }

    private fun erVedlikeholdsmodusPåslått(): Boolean = kravService.erVedlikeholdsmodusPåslått()
}
