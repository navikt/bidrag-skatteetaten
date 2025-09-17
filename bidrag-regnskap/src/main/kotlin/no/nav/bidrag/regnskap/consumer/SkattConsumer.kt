package no.nav.bidrag.regnskap.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.commons.security.maskinporten.MaskinportenClient
import no.nav.bidrag.transport.regnskap.behandlingsstatus.BehandlingsstatusResponse
import no.nav.bidrag.transport.regnskap.krav.Kravliste
import no.nav.bidrag.transport.regnskap.vedlikeholdsmodus.Vedlikeholdsmodus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.net.URI

private val LOGGER = KotlinLogging.logger { }

@Service
class SkattConsumer(
    @param:Value("\${SKATT_URL}") private val skattUrl: String,
    @param:Value("\${maskinporten.scope}") private val scope: String,
    @param:Qualifier("regnskap") private val restTemplate: RestTemplate,
    private val maskinportenClient: MaskinportenClient,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        const val KRAV_PATH = "/api/krav"
        const val LIVENESS_PATH = "/api/liveness"
        const val VEDLIKEHOLDSMODUS_PATH = "/api/vedlikeholdsmodus"
        private const val VEDLIKEHOLDSMODUS_CACHE_NAME = "vedlikeholdsmodus_cache"
    }

    fun sendKrav(kravliste: Kravliste): ResponseEntity<String> {
        LOGGER.info { "Overfører krav til skatt:\n${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(kravliste)}" }

        return utførHttpKall(
            url = opprettSkattUrl(KRAV_PATH),
            method = HttpMethod.POST,
            requestBody = kravliste,
            responseType = String::class.java,
        )
    }

    @CacheEvict(value = [VEDLIKEHOLDSMODUS_CACHE_NAME], allEntries = true)
    fun oppdaterVedlikeholdsmodus(vedlikeholdsmodus: Vedlikeholdsmodus): ResponseEntity<Any> {
        LOGGER.info { "Oppdaterer vedlikeholdsmodus til: $vedlikeholdsmodus" }

        return utførHttpKall(
            url = opprettSkattUrl(VEDLIKEHOLDSMODUS_PATH),
            method = HttpMethod.POST,
            requestBody = vedlikeholdsmodus,
            responseType = Any::class.java,
        )
    }

    @Cacheable(value = [VEDLIKEHOLDSMODUS_CACHE_NAME], key = "#root.methodName")
    fun hentStatusPåVedlikeholdsmodus(): ResponseEntity<Any> = utførHttpKall(
        url = opprettSkattUrl(LIVENESS_PATH),
        method = HttpMethod.GET,
        requestBody = null,
        responseType = Any::class.java,
    )

    fun sjekkBehandlingsstatus(batchUid: String): ResponseEntity<BehandlingsstatusResponse> = utførHttpKall(
        url = opprettSkattUrl("$KRAV_PATH/$batchUid"),
        method = HttpMethod.GET,
        requestBody = null,
        responseType = BehandlingsstatusResponse::class.java,
    )

    private fun <T, R> utførHttpKall(
        url: URI,
        method: HttpMethod,
        requestBody: T?,
        responseType: Class<R>,
    ): ResponseEntity<R> = restTemplate.exchange(
        url,
        method,
        HttpEntity(requestBody, opprettHttpHeaders()),
        responseType,
    )

    private fun opprettSkattUrl(path: String): URI = URI.create(skattUrl + path)

    private fun opprettHttpHeaders(): HttpHeaders = HttpHeaders().apply {
        set("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        set("Accept", MediaType.APPLICATION_JSON_VALUE)
        set("Authorization", "Bearer " + hentJwtToken())
    }

    private fun hentJwtToken(): String = maskinportenClient.hentMaskinportenToken(scope).parsedString
}
