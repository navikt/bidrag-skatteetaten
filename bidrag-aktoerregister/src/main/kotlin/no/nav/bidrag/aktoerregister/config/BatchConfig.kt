package no.nav.bidrag.aktoerregister.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import javax.sql.DataSource

@Configuration
class BatchConfig {

    @Bean
    fun lockProvider(dataSource: DataSource): LockProvider = JdbcTemplateLockProvider(dataSource, "aktoerregister.shedlock")

    @Bean
    fun taskExecutor(): TaskExecutor = SimpleAsyncTaskExecutor()
}
