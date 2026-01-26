package no.nav.bidrag.aktoerregister.batch.person

import no.nav.bidrag.aktoerregister.batch.AktørBatchProcessorResult
import no.nav.bidrag.aktoerregister.batch.AktørBatchWriter
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.parameters.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@EnableBatchProcessing
class PersonBatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    @Qualifier("personBatchReader") private val personBatchReader: RepositoryItemReader<Aktør>,
    private val aktørBatchWriter: AktørBatchWriter,
    private val personBatchProcessor: PersonBatchProcessor,
) {

    companion object {
        const val PERSON_BATCH_OPPDATERING_JOB = "PERSON_BATCH_OPPDATERING_JOB"
        const val PERSON_OPPDATER_AKTOERER_STEP = "PERSON_OPPDATER_AKTOERER_STEP"
    }

    @Bean
    fun personJob(): Job = JobBuilder(PERSON_BATCH_OPPDATERING_JOB, jobRepository)
        .listener(PersonJobListener())
        .incrementer(RunIdIncrementer())
        .flow(personStep())
        .end()
        .build()

    @Bean
    fun personStep(): Step = StepBuilder(PERSON_OPPDATER_AKTOERER_STEP, jobRepository)
        .chunk<Aktør, AktørBatchProcessorResult>(100, transactionManager) //TODO(Spring boot 4)
        .reader(personBatchReader)
        .processor(personBatchProcessor)
        .writer(aktørBatchWriter)
        .build()
}
