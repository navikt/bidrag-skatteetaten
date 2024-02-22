package no.nav.bidrag.aktoerregister.batch.samhandler

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.text.SimpleDateFormat
import java.util.Calendar

@Configuration
@EnableScheduling
class SamhandlerBatchScheduler(
    private val jobLauncher: JobLauncher,
    @Qualifier("samhandlerJob") private val job: Job,
) {

    @Scheduled(cron = "0 0 20 * * *")
    @SchedulerLock(name = SamhandlerBatchConfig.SAMHANDLER_BATCH_OPPDATERING_JOB, lockAtMostFor = "30m", lockAtLeastFor = "5m")
    fun scheduleSamhandlerBatch() {
        val jobParameters = JobParametersBuilder()
            .addString("time", SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time))
            .toJobParameters()
        jobLauncher.run(job, jobParameters)
    }
}
