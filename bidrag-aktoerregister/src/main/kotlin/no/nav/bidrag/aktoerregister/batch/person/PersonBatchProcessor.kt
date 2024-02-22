package no.nav.bidrag.aktoerregister.batch.person

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.aktoerregister.SECURE_LOGGER
import no.nav.bidrag.aktoerregister.batch.AktørBatchProcessorResult
import no.nav.bidrag.aktoerregister.batch.AktørStatus
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import no.nav.bidrag.aktoerregister.service.AktørService
import no.nav.bidrag.domene.ident.Ident
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

private val LOGGER = KotlinLogging.logger { }

@Component
class PersonBatchProcessor(
    private val aktørService: AktørService,
) : ItemProcessor<Aktør, AktørBatchProcessorResult?> {

    override fun process(aktør: Aktør): AktørBatchProcessorResult? {
        return try {
            aktørService.hentAktørFraPerson(Ident(aktør.aktørIdent))
                .takeIf { it != aktør }
                ?.let {
                    val originalIdent = if (it.aktørIdent != aktør.aktørIdent) aktør.aktørIdent else null
                    AktørBatchProcessorResult(aktør, it, AktørStatus.UPDATED, originalIdent)
                }
        } catch (e: Exception) {
            SECURE_LOGGER.error("Person: ${aktør.aktørIdent} feilet i PersonBatchProcessor. Feilmelding: ${e.message}")
            LOGGER.error(e) { e.message }
            null
        }
    }
}
