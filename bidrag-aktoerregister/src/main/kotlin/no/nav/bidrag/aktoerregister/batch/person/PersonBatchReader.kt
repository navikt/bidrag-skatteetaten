package no.nav.bidrag.aktoerregister.batch.person

import no.nav.bidrag.aktoerregister.dto.enumer.Identtype
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import no.nav.bidrag.aktoerregister.persistence.repository.AktørRepository
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort

@Configuration
class PersonBatchReader {
    @Bean
    fun personBatchReader(aktoerRepository: AktørRepository): RepositoryItemReader<Aktør> = RepositoryItemReaderBuilder<Aktør>()
        .name("personBatchReader")
        .repository(aktoerRepository)
        .methodName("findAllByAktørType")
        .arguments(listOf(Identtype.PERSONNUMMER.name))
        .pageSize(100)
        .sorts(mapOf("aktørIdent" to Sort.Direction.ASC))
        .build()
}
