package no.nav.bidrag.aktoerregister.consumer

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.aktoerregister.SECURE_LOGGER
import no.nav.bidrag.aktoerregister.exception.AktørNotFoundException
import no.nav.bidrag.aktoerregister.util.ConsumerUtils.leggTilPathPåUri
import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.transport.samhandler.SamhandlerDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestOperations
import java.net.URI

private val LOGGER = KotlinLogging.logger {}

@Service
class SamhandlerConsumer(
    @Value("\${BIDRAG_SAMHANDLER_URL}") val url: URI,
    @Qualifier("azure") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "bidrag-aktoerregister-samhandler") {

    companion object {
        private const val SAMHANDLER_PATH = "/samhandler"
    }

    fun hentSamhandler(aktørIdent: Ident): SamhandlerDto? {
        try {
            val response: SamhandlerDto? = postForEntity(leggTilPathPåUri(url, SAMHANDLER_PATH), aktørIdent)
            LOGGER.debug { "Hentet samhandler med $aktørIdent fra bidrag-samhandler." }
            SECURE_LOGGER.info("Hentet samhandler med id: ${aktørIdent.verdi} fra bidrag-samhandler.")
            return response
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                throw AktørNotFoundException("Aktør ikke funnet i bidrag-samhandler")
            }
            throw e
        } catch (e: Exception) {
            SECURE_LOGGER.error(
                "Noe gikk galt i kallet mot bidrag-samhandler for ident: ${aktørIdent.verdi}. Svaret fra bidrag-samhandler var: ${e.message}",
            )
            throw e
        }
    }
}
