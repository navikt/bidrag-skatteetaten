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
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Fant ingen bidragssak på saksnummeret.", content = [Content()]),
        ],
    )
    fun hentInnkrevingssakPåBidragssak(@RequestBody saksnummerRequest: SaksnummerRequest): ResponseEntity<BidragssakDto?> {
        if (reskontroLegacyEnabled) {
            return reskontroLegacyService.hentInnkrevingssakPåSak(saksnummerRequest)
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
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Fant ingen bidragssak på identen.", content = [Content()]),

        ],
    )
    fun hentInnkrevingssakPåBidragssak(@RequestBody personRequest: PersonRequest): ResponseEntity<BidragssakMedSkyldnerDto?> {
        if (reskontroLegacyEnabled) {
            return reskontroLegacyService.hentInnkrevingssakPåPerson(personRequest)
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
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Fant ingen transaksjoner på saksnummeret.", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåBidragssak(@RequestBody saksnummerRequest: SaksnummerRequest): ResponseEntity<TransaksjonerDto?> {
        if (reskontroLegacyEnabled) {
            return reskontroLegacyService.hentTransaksjonerPåBidragssak(saksnummerRequest)
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
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Fant ingen transaksjoner på identen.", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåPerson(@RequestBody personRequest: PersonRequest): ResponseEntity<TransaksjonerDto?> {
        if (reskontroLegacyEnabled) {
            return reskontroLegacyService.hentTransaksjonerPåPerson(personRequest)
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
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Fant ingen transaksjoner for transaksjonsid.", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåTransaksjonsid(@RequestParam transaksjonsid: Long): ResponseEntity<TransaksjonerDto?> {
        if (reskontroLegacyEnabled) {
            return reskontroLegacyService.hentTransaksjonerPåTransaksjonsid(transaksjonsid)
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
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Fant ingen informasjon om innkrevingssaken på identen.", content = [Content()]),
        ],
    )
    fun hentInformasjonOmInnkrevingssaken(@RequestBody personRequest: PersonRequest): ResponseEntity<InnkrevingssaksinformasjonDto?> {
        if (reskontroLegacyEnabled) {
            return reskontroLegacyService.hentInformasjonOmInnkrevingssaken(personRequest)
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
        ],
    )
    fun endreRmForSak(@RequestBody endreRmForSak: EndreRmForSakRequest) {
        if (reskontroLegacyEnabled) {
            reskontroLegacyService.endreRmForSak(endreRmForSak)
            return
        }
        reskontroService.endreRmForSak(endreRmForSak.saksnummer, endreRmForSak.barn, endreRmForSak.nyttFødselsnummer)
    }

    @PostMapping("/ip")
    fun hentIp(): String = try {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.ipify.org"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        "My current IP address is ${response.body()}"
    } catch (e: IOException) {
        e.printStackTrace()
        "Unable to fetch IP address"
    }

    @PostMapping("/honucity")
    fun hentHonuCity(): String = try {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://echo.honu.city/"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        "Honu.city response: ${response.body()}"
    } catch (e: IOException) {
        e.printStackTrace()
        "Unable to fetch Honu.city response"
    }
}
