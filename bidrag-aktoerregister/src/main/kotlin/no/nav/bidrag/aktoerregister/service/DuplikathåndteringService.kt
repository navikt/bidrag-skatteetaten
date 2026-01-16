package no.nav.bidrag.aktoerregister.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import no.nav.bidrag.aktoerregister.persistence.repository.AktørRepository
import no.nav.bidrag.aktoerregister.persistence.repository.HendelseRepository
import no.nav.bidrag.aktoerregister.persistence.repository.TidligereIdenterRepository
import org.springframework.stereotype.Service

private val LOGGER = KotlinLogging.logger {}

@Service
class DuplikathåndteringService(
    private val aktørRepository: AktørRepository,
    private val hendelseRepository: HendelseRepository,
    private val tidligereIdenterRepository: TidligereIdenterRepository,
    private val entityManager: EntityManager,
) {

    /**
     * Finner alle aktører i databasen som matcher noen av de tidligere identene.
     * Returnerer en liste med unike aktører sortert etter id (eldste først).
     */
    fun finnAlleMatchendeAktører(tidligereIdenter: Set<String>): List<Aktør> {
        if (tidligereIdenter.isEmpty()) return emptyList()

        return tidligereIdenter
            .mapNotNull { ident ->
                aktørRepository.findByAktørIdent(ident)
            }
            .distinctBy { it.id }
            .sortedBy { it.id }
    }

    /**
     * Velger den primære aktøren fra en liste av matchende aktører.
     * Returnerer den eldste aktøren (laveste id) som den primære.
     */
    fun velgPrimærAktør(matchendeAktører: List<Aktør>): Aktør = matchendeAktører.minBy { it.id!! }

    /**
     * Sletter alle duplikat-aktører bortsett fra den primære.
     * Sletter også alle relaterte hendelser og tidligere identer.
     */
    fun slettDuplikater(primærAktør: Aktør, alleMachendeAktører: List<Aktør>) {
        val duplikater = alleMachendeAktører.filter { it.id != primærAktør.id }

        if (duplikater.isEmpty()) return

        duplikater.forEach { duplikat ->
            LOGGER.info { "Sletter duplikat aktør med ident: ${duplikat.aktørIdent}, id: ${duplikat.id}" }
            slettAktør(duplikat)
        }

        entityManager.flush()
    }

    /**
     * Sletter en aktør med alle relaterte entiteter.
     */
    private fun slettAktør(aktør: Aktør) {
        // Slett hendelser manuelt for å unngå cascade-problemer
        aktør.hendelser.forEach { hendelse ->
            hendelseRepository.delete(hendelse)
        }

        aktør.dødsbo

        // Slett tidligere identer manuelt
        aktør.tidligereIdenter.forEach { tidligereIdent ->
            tidligereIdenterRepository.delete(tidligereIdent)
        }

        // Slett selve aktøren
        aktørRepository.delete(aktør)
    }
}
