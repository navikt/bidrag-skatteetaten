package no.nav.bidrag.regnskap.config

import no.nav.bidrag.regnskap.SECURE_LOGGER
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.util.backoff.ExponentialBackOff

private val LOGGER = LoggerFactory.getLogger(KafkaConfiguration::class.java)

@Configuration
class KafkaConfiguration {

    companion object {
        private const val BACKOFF_MULTIPLIER = 1.2
        private const val MAX_INTERVAL_MS = 300000L // 5 mins
    }

    @Bean
    fun defaultErrorHandler(): DefaultErrorHandler {
        val errorHandler = opprettErrorHandler()
        errorHandler.setRetryListeners(KafkaRetryListener())
        return errorHandler
    }

    private fun opprettErrorHandler(): DefaultErrorHandler = DefaultErrorHandler({ rec, e ->
        val key = rec.key()
        val value = rec.value()
        val offset = rec.offset()
        val topic = rec.topic()
        val partition = rec.partition()
        SECURE_LOGGER.error(
            "Kafka melding med nøkkel $key, partition $partition og topic $topic feilet på offset $offset. Melding som feilet: $value",
            e,
        )
    }, opprettBackoffPolicy())

    private fun opprettBackoffPolicy(): ExponentialBackOff = ExponentialBackOff().apply {
        multiplier = BACKOFF_MULTIPLIER
        maxInterval = MAX_INTERVAL_MS
    }
}
