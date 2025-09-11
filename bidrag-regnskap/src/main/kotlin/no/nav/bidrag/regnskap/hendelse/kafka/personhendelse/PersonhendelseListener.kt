package no.nav.bidrag.regnskap.hendelse.kafka.personhendelse

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.regnskap.service.HendelseService
import no.nav.bidrag.transport.person.hendelse.Endringsmelding
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

private val LOGGER = KotlinLogging.logger { }

@Component
class PersonhendelseListener(
    private val hendelseService: HendelseService,
    private val objectMapper: ObjectMapper,
) {
    @KafkaListener(
        groupId = "\${PERSON_HENDELSE_KAFKA_GROUP_ID_SISTE}",
        topics = ["\${TOPIC_PERSONHENDELSE}"],
        properties = ["auto.offset.reset=latest"],
    )
    fun lesHendelse(
        hendelse: String,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        acknowledgment: Acknowledgment,
    ) {
        logKafkaMetadata(topic, offset, partition, groupId)

        val endringsmelding = deserialiserEndringsmelding(hendelse)
        hendelseService.behandlePersonhendelse(endringsmelding)
        acknowledgment.acknowledge()
    }

    private fun logKafkaMetadata(topic: String, offset: Long, partition: Int, groupId: String) {
        LOGGER.debug { "Leser hendelse fra topic: $topic, offset: $offset, partition: $partition, groupId: $groupId" }
    }

    private fun deserialiserEndringsmelding(hendelse: String): Endringsmelding = objectMapper.readValue(hendelse, Endringsmelding::class.java)
}
