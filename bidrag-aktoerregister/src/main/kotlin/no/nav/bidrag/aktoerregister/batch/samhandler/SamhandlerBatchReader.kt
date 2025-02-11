package no.nav.bidrag.aktoerregister.batch.samhandler

import no.nav.bidrag.aktoerregister.dto.enumer.Identtype
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import no.nav.bidrag.aktoerregister.persistence.repository.AktørRepository
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Collections

@Component
class SamhandlerBatchReader(aktoerRepository: AktørRepository) :
    RepositoryItemReader<Aktør>(),
    ItemReader<Aktør> {
    init {
        this.setRepository(aktoerRepository)
        this.setMethodName("findAllByAktørType")
        this.setArguments(listOf(Identtype.AKTOERNUMMER.name))
        this.setPageSize(100)
        this.setSort(Collections.singletonMap("aktørIdent", Sort.Direction.ASC))
    }
}
