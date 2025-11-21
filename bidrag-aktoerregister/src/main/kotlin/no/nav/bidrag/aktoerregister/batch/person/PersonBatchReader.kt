package no.nav.bidrag.aktoerregister.batch.person

import no.nav.bidrag.aktoerregister.dto.enumer.Identtype
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import no.nav.bidrag.aktoerregister.persistence.repository.AktørRepository
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.*

@Component
class PersonBatchReader(aktoerRepository: AktørRepository) : RepositoryItemReader<Aktør>() {
    init {
        this.setRepository(aktoerRepository)
        this.setMethodName("findAllByAktørType")
        this.setArguments(listOf(Identtype.PERSONNUMMER.name))
        this.setPageSize(100)
        this.setSort(Collections.singletonMap("aktørIdent", Sort.Direction.ASC))
    }
}
