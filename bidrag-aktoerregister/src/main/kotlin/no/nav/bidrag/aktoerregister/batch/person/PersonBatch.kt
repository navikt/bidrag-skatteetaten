package no.nav.bidrag.aktoerregister.batch.person

import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.Calendar

@Component
class PersonBatch(
    private val jobLauncher: JobLauncher, //TODO(Spring boot 4)
    @param:Qualifier("personJob") private val job: Job,
) {

    fun startPersonBatch() {
        val jobParameters = JobParametersBuilder()
            .addString("time", SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time))
            .toJobParameters()
        jobLauncher.run(job, jobParameters)
    }
}
