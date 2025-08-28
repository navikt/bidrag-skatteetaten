package no.nav.bidrag.reskontro.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.reskontro.service.ReskontroLegacyService
import no.nav.bidrag.reskontro.service.ReskontroService
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.EndreRmForSakRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakMedSkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssaksinformasjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class ReskontroController(
    private val reskontroService: ReskontroService,
    private val reskontroLegacyService: ReskontroLegacyService,
    @Value("#{new Boolean('\${RESKONTRO_LEGACY_ENABLED}')}") private val reskontroLegacyEnabled: Boolean,
) {
    @PostMapping("/innkrevningssak/bidragssak")
    @Operation(
        description = "Henter saksinformasjon om bidragssaken",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet saksinformasjon om bidragssaken"),
            ApiResponse(responseCode = "204", description = "Fant ingen bidragssak på saksnummeret.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "504", description = "Timeout mot skatt", content = [Content()]),
        ],
    )
    fun hentInnkrevingssakPåBidragssak(@RequestBody saksnummerRequest: SaksnummerRequest): ResponseEntity<BidragssakDto?> {
        if (reskontroLegacyEnabled) {
            val legacyResponse = reskontroLegacyService.hentInnkrevingssakPåSak(saksnummerRequest)
            if (legacyResponse == null) {
                return ResponseEntity.notFound().build()
            }
            return ResponseEntity.ok(legacyResponse)
        }
        val innkrevingssakPåSak = reskontroService.hentInnkrevingssakPåSak(saksnummerRequest)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(innkrevingssakPåSak)
    }

    @PostMapping("/innkrevningssak/person")
    @Operation(
        description = "Henter saksinformasjon om bidragssaker på personen",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet saksinformasjon om bidragssaker på personen"),
            ApiResponse(responseCode = "204", description = "Fant ingen bidragssak på identen.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "504", description = "Timeout mot skatt", content = [Content()]),

        ],
    )
    fun hentInnkrevingssakPåPerson(@RequestBody personRequest: PersonRequest): ResponseEntity<BidragssakMedSkyldnerDto?> {
        if (reskontroLegacyEnabled) {
            val legacyResponse = reskontroLegacyService.hentInnkrevingssakPåPerson(personRequest)
            if (legacyResponse == null) {
                return ResponseEntity.notFound().build()
            }
            return ResponseEntity.ok(legacyResponse)
        }
        val innkrevingssakPåPerson = reskontroService.hentInnkrevingssakPåPerson(personRequest)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(innkrevingssakPåPerson)
    }

    @PostMapping("/transaksjoner/bidragssak")
    @Operation(
        description = "Henter transaksjoner for bidragssaken",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet transaksjoner for bidragssaken"),
            ApiResponse(responseCode = "204", description = "Fant ingen transaksjoner på saksnummeret.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "504", description = "Timeout mot skatt", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåBidragssak(@RequestBody saksnummerRequest: SaksnummerRequest): ResponseEntity<TransaksjonerDto?> {
        if (reskontroLegacyEnabled) {
            val legacyResponse = reskontroLegacyService.hentTransaksjonerPåBidragssak(saksnummerRequest)
            if (legacyResponse == null) {
                return ResponseEntity.notFound().build()
            }
            return ResponseEntity.ok(legacyResponse)
        }
        val transaksjonerPåBidragssak = reskontroService.hentTransaksjonerPåBidragssak(saksnummerRequest)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(transaksjonerPåBidragssak)
    }

    @PostMapping("/transaksjoner/person")
    @Operation(
        description = "Henter transaksjoner for person",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet transaksjoner for person"),
            ApiResponse(responseCode = "201", description = "Fant ingen transaksjoner på identen.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "504", description = "Timeout mot skatt", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåPerson(@RequestBody personRequest: PersonRequest): ResponseEntity<TransaksjonerDto?> {
        if (reskontroLegacyEnabled) {
            val legacyResponse = reskontroLegacyService.hentTransaksjonerPåPerson(personRequest)
            if (legacyResponse == null) {
                return ResponseEntity.notFound().build()
            }
            return ResponseEntity.ok(legacyResponse)
        }
        val transaksjonerPåPerson = reskontroService.hentTransaksjonerPåPerson(personRequest)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(transaksjonerPåPerson)
    }

    @GetMapping("/transaksjoner/transaksjonsid")
    @Operation(
        description = "Henter transaksjoner på transaksjonsid",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet transaksjoner på transaksjonsid"),
            ApiResponse(responseCode = "204", description = "Fant ingen transaksjoner for transaksjonsid.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "504", description = "Timeout mot skatt", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåTransaksjonsid(@RequestParam transaksjonsid: Long): ResponseEntity<TransaksjonerDto?> {
        if (reskontroLegacyEnabled) {
            val legacyResponse = reskontroLegacyService.hentTransaksjonerPåTransaksjonsid(transaksjonsid)
            if (legacyResponse == null) {
                return ResponseEntity.notFound().build()
            }
            return ResponseEntity.ok(legacyResponse)
        }
        val transaksjonerPåTransaksjonsid = reskontroService.hentTransaksjonerPåTransaksjonsid(transaksjonsid)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(transaksjonerPåTransaksjonsid)
    }

    @PostMapping("/innkrevingsinformasjon")
    @Operation(
        description = "Henter informasjon om innkrevingssaken knyttet til person",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet informasjon om innkrevingssaken knyttet til person"),
            ApiResponse(responseCode = "204", description = "Fant ingen informasjon om innkrevingssaken på identen.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "504", description = "Timeout mot skatt", content = [Content()]),
        ],
    )
    fun hentInformasjonOmInnkrevingssaken(@RequestBody personRequest: PersonRequest): ResponseEntity<InnkrevingssaksinformasjonDto?> {
        if (reskontroLegacyEnabled) {
            val legacyResponse = reskontroLegacyService.hentInformasjonOmInnkrevingssaken(personRequest)
            if (legacyResponse == null) {
                return ResponseEntity.notFound().build()
            }
            return ResponseEntity.ok(legacyResponse)
        }
        val informasjonOmInnkrevingssaken = reskontroService.hentInformasjonOmInnkrevingssaken(personRequest)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(informasjonOmInnkrevingssaken)
    }

    @PatchMapping("/endreRmForSak")
    @Operation(
        description = "Endrer rm for sak",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Endret rm for sak"),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "504", description = "Timeout mot skatt", content = [Content()]),
        ],
    )
    fun endreRmForSak(@RequestBody endreRmForSak: EndreRmForSakRequest) {
        if (reskontroLegacyEnabled) {
            reskontroLegacyService.endreRmForSak(endreRmForSak)
            return
        }
        reskontroService.endreRmForSak(endreRmForSak.saksnummer, endreRmForSak.barn, endreRmForSak.nyttFødselsnummer)
    }
}
