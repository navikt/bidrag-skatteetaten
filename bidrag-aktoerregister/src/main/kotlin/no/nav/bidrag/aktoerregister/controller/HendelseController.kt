package no.nav.bidrag.aktoerregister.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.nav.bidrag.aktoerregister.service.PersonHendelseService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class HendelseController(
    private val personHendelseService: PersonHendelseService,
) {
    @Operation(
        summary = "Start manuell innlesing av en hendelse.",
        description = "Hendelser skal automatisk behandles. Dette endepunktet er ment for testtilfeller.",
    )
    @ApiResponse(responseCode = "200", description = "Hendelse har blitt behandlet.")
    @PostMapping("/hendelse")
    fun startSamhandlerBatch(hendelse: String): ResponseEntity<*> {
        personHendelseService.behandleHendelse(hendelse)
        return ResponseEntity.ok().build<Any>()
    }
}
