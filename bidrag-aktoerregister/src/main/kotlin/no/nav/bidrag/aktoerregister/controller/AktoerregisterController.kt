package no.nav.bidrag.aktoerregister.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.aktoerregister.SECURE_LOGGER
import no.nav.bidrag.aktoerregister.dto.AktoerDTO
import no.nav.bidrag.aktoerregister.dto.AktoerIdDTO
import no.nav.bidrag.aktoerregister.dto.HendelseDTO
import no.nav.bidrag.aktoerregister.exception.AktørNotFoundException
import no.nav.bidrag.aktoerregister.service.AktørService
import no.nav.bidrag.aktoerregister.service.HendelseService
import no.nav.bidrag.transport.samhandler.SamhandlerSøk
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

private val LOGGER = KotlinLogging.logger {}

@RestController
@ProtectedWithClaims(issuer = "maskinporten", claimMap = ["scope=nav:bidrag:aktoerregister.read"])
class AktoerregisterController(
    private val aktørService: AktørService,
    private val hendelseService: HendelseService,
) {

    @Operation(
        summary = "Hent informasjon om gitt aktør.",
        description = "For personer returneres kun kontonummer. For andre typer aktører leveres også navn og adresse.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Aktøren ble funnet."),
        ApiResponse(responseCode = "400", description = "Gitt identtype eller ident er ugyldig.", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Ingen aktør med gitt identtype og ident ble funnet.", content = [Content()]),
    )
    @PostMapping(path = ["/aktoer"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentAktoer(@RequestBody request: AktoerIdDTO, @RequestParam(required = false) tvingOppdatering: Boolean = false): ResponseEntity<AktoerDTO> {
        return try {
            SECURE_LOGGER.info("Kall mot /aktoer for å hente ut aktør: Type: ${request.identtype.name} Id: ${request.aktoerId}")
            val aktoer = aktørService.hentAktoer(request, tvingOppdatering)
            ResponseEntity.ok(aktoer)
        } catch (e: AktørNotFoundException) {
            LOGGER.info { "Aktør ${request.aktoerId} ikke funnet." }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Finner ingen aktør med oppgitt ident", e)
        } catch (e: Exception) {
            LOGGER.error(e) { "Feil ved henting av aktør ${request.aktoerId}. Feilmelding: ${e.message}" }
            SECURE_LOGGER.error("Feil ved henting av aktør ${request.aktoerId}. Feilmelding: ${e.message}")
            throw ResponseStatusException(INTERNAL_SERVER_ERROR, "Intern tjenestefeil. Feil ved henting av aktør. Prøv igjen senere.", e)
        }
    }

    @Operation(
        summary = "Tilbyr en liste over aktøroppdateringer.",
        description = "Ingen informasjon om aktøren leveres av denne tjenesten utover aktørIden\n." +
            "Hendelsene legges inn med stigende sekvensnummer. Klienten må selv ta vare på hvilke sekvensnummer som sist er behandlet, og be om å få hendelser fra det neste sekvensnummeret ved neste kall.\n" +
            "Dersom det ikke returneres noen hendelser er ingen av aktørene endret siden siste kall. Samme sekvensnummer må da benyttes i neste kall.\n\n" +
            "Nye hendelser vil alltid ha høyere sekvensnummer enn tidligere hendelser.\n" +
            "Det kan forekomme hull i sekvensnummer-rekken.\n" +
            "Dersom det kommer en hendelse for en aktør med tidligere hendelser (lavere sekvensnummer) er det ikke garantert at de tidligere hendelsene ikke returneres.",
    )
    @GetMapping(path = ["/hendelser"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentHendelser(
        @Parameter(description = "Angir første sekvensnummer som ønskes hentet. Default-verdi er 0")
        @RequestParam(name = "fraSekvensnummer", defaultValue = "0")
        fraSekvensnummer: Int = 0,
        @Parameter(description = "Maksimalt antall hendelser som ønskes hentet. Default-verdi er 1000.")
        @RequestParam(name = "antall", defaultValue = "1000")
        antall: Int = 1000,
    ): ResponseEntity<List<HendelseDTO>> {
        return try {
            ResponseEntity.ok(hendelseService.hentHendelser(fraSekvensnummer, antall))
        } catch (e: Exception) {
            LOGGER.error(e) { "Feil ved henting av $antall hendelser fra sekvensnummer $fraSekvensnummer" }
            throw ResponseStatusException(INTERNAL_SERVER_ERROR, "Intern tjenestefeil. Problem ved henting av hendelser. Prøv igjen senere", e)
        }
    }

    @Operation(
        summary = "Avmelder gitt aktør.",
        description = "Avmelder aktøren fra aktørregisteret. Hendelser vil ikke lenger bli opprettet på denne aktøren.",
    )
    @PostMapping(path = ["/avmelding"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Aktøren ble avmeldt."),
        ApiResponse(responseCode = "400", description = "Gitt identtype eller ident er ugyldig.", content = [Content()]),
    )
    fun avmeldAktør(@RequestBody request: AktoerIdDTO): ResponseEntity<Any> {
        return try {
            aktørService.slettAktoer(request)
            ResponseEntity.ok().build()
        } catch (e: AktørNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/samhandlersok")
    @Operation(
        description = "Søker etter samhandlere.",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun samhandlerSøk(samhandlerSøk: SamhandlerSøk): SamhandlersøkeresultatDto {
        return aktørService.samhandlerSøk(samhandlerSøk)
    }
}
