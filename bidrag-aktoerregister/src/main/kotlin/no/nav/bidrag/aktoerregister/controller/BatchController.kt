package no.nav.bidrag.aktoerregister.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.nav.bidrag.aktoerregister.batch.person.PersonBatch
import no.nav.bidrag.aktoerregister.batch.samhandler.SamhandlerBatch
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

private val LOGGER = KotlinLogging.logger {}

@RestController
@Protected
class BatchController(private val samhandlerBatch: SamhandlerBatch, private val personBatch: PersonBatch) {

    @Operation(
        summary = "Start kjøring av Samhandler batch.",
        description = "Samhandler batchen startes asynkront. Dette vil medføre at feil under kjøring av batchen ikke vil reflekteres i responskoden dette endepunktet returnerer.",
    )
    @ApiResponse(responseCode = "200", description = "Samhandler batchen ble startet.")
    @PostMapping("/samhandlerBatch")
    fun startSamhandlerBatch(): ResponseEntity<*> {
        CompletableFuture.runAsync {
            try {
                samhandlerBatch.startSamhandlerBatch()
            } catch (e: Exception) {
                LOGGER.error(e) { "Manuell start av batchen feilet med følgende feilkode: ${e.message}" }
            }
        }
        return ResponseEntity.ok().build<Any>()
    }

    @Operation(
        summary = "Start kjøring av Person batch.",
        description = "Person batchen startes asynkront. Dette vil medføre at feil under kjøring av batchen ikke vil reflekteres i responskoden dette endepunktet returnerer.",
    )
    @ApiResponse(responseCode = "200", description = "Person batchen ble startet.")
    @PostMapping("/personBatch")
    fun startPersonBatch(): ResponseEntity<*> {
        CompletableFuture.runAsync {
            try {
                personBatch.startPersonBatch()
            } catch (e: Exception) {
                LOGGER.error(e) { "Manuell start av batchen feilet med følgende feilkode: ${e.message}" }
            }
        }
        return ResponseEntity.ok().build<Any>()
    }
}
