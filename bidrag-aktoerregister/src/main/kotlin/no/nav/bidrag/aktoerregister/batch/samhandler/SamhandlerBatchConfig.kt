package no.nav.bidrag.aktoerregister.batch.samhandler

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
class SamhandlerBatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    @param:Qualifier("samhandlerBatchReader") private val samhandlerBatchReader: RepositoryItemReader<Aktør>,
    private val aktørBatchWriter: AktørBatchWriter,
    private val samhandlerBatchProcessor: SamhandlerBatchProcessor,
) {

    companion object {
        const val SAMHANDLER_BATCH_OPPDATERING_JOB = "SAMHANDLER_BATCH_OPPDATERING_JOB"
        const val SAMHANDLER_OPPDATER_AKTOERER_STEP = "SAMHANDLER_OPPDATER_AKTOERER_STEP"
    }

    @Bean
    fun samhandlerJob(): Job = JobBuilder(SAMHANDLER_BATCH_OPPDATERING_JOB, jobRepository)
        .listener(SamhandlerJobListener())
        .incrementer(RunIdIncrementer())
        .flow(samhandlerStep())
        .end()
        .build()

    @Bean
    fun samhandlerStep(): Step = StepBuilder(SAMHANDLER_OPPDATER_AKTOERER_STEP, jobRepository)
        .chunk<Aktør, AktørBatchProcessorResult>(100, transactionManager) //TODO(Spring boot 4)
        .reader(samhandlerBatchReader)
        .processor(samhandlerBatchProcessor)
        .writer(aktørBatchWriter)
        .build()
}
