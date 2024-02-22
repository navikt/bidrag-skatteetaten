package no.nav.bidrag.aktoerregister.batch

import no.nav.bidrag.aktoerregister.service.AktørService
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class AktørBatchWriter(private val aktørService: AktørService) : ItemWriter<AktørBatchProcessorResult> {

    override fun write(chunk: Chunk<out AktørBatchProcessorResult>) {
        val slettedeAktører: MutableList<String> = mutableListOf()
        chunk
            .filter { it.aktørStatus == AktørStatus.UPDATED }
            .forEach {
                if (!slettedeAktører.contains(it.aktør.aktørIdent)) {
                    aktørService.oppdaterAktør(it.aktør, it.nyAktør, it.originalIdent)?.let { slettedeAktører.add(it) }
                }
            }
    }
}
