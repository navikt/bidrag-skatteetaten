package no.nav.bidrag.aktoerregister.batch.samhandler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterJob

private val LOGGER = KotlinLogging.logger { }

class SamhandlerJobListener {

    @AfterJob
    fun afterJob(jobExecution: JobExecution) {
        jobExecution.stepExecutions.first { stepExecution: StepExecution -> stepExecution.stepName == SamhandlerBatchConfig.SAMHANDLER_OPPDATER_AKTOERER_STEP }
            .let { stepExecution ->
                LOGGER.info {
                    "Av totalt ${stepExecution.readCount} samhandler-aktører var ${stepExecution.writeCount} blitt endret siden forrige kjøring. " +
                        "${stepExecution.writeCount} aktører ble dermed oppdatert i databasen."
                }
            }
    }
}
