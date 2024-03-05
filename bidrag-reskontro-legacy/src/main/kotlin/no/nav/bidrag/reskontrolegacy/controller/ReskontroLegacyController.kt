package no.nav.bidrag.reskontrolegacy.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.reskontrolegacy.service.ReskontroLegacyService
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.EndreRmForSakRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssak.BidragssakMedSkyldnerDto
import no.nav.bidrag.transport.reskontro.response.innkrevingssaksinformasjon.InnkrevingssaksinformasjonDto
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonerDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Protected
class ReskontroLegacyController(private val reskontroLegacyService: ReskontroLegacyService) {

    @PostMapping("/innkrevningssak/bidragssak")
    @Operation(
        description = "Henter saksinformasjon om bidragssaken",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet saksinformasjon om bidragssaken"),
            ApiResponse(responseCode = "400", description = "Feil i forespørselen", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
        ],
    )
    fun hentInnkrevingssakPåBidragssak(@RequestBody saksnummerRequest: SaksnummerRequest): BidragssakDto {
        return reskontroLegacyService.hentInnkrevingssakPåSak(saksnummerRequest)
    }

    @PostMapping("/innkrevningssak/person")
    @Operation(
        description = "Henter saksinformasjon om bidragssaker på personen",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet saksinformasjon om bidragssaker på personen"),
            ApiResponse(responseCode = "204", description = "Fant ingen data", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Feil i forespørselen", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
        ],
    )
    fun hentInnkrevingssakPåBidragssak(@RequestBody personRequest: PersonRequest): BidragssakMedSkyldnerDto {
        return reskontroLegacyService.hentInnkrevingssakPåPerson(personRequest)
    }

    @PostMapping("/transaksjoner/bidragssak")
    @Operation(
        description = "Henter transaksjoner for bidragssaken",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet transaksjoner for bidragssaken"),
            ApiResponse(responseCode = "204", description = "Fant ingen data", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Feil i forespørselen", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåBidragssak(@RequestBody saksnummerRequest: SaksnummerRequest): TransaksjonerDto {
        return reskontroLegacyService.hentTransaksjonerPåBidragssak(saksnummerRequest)
    }

    @PostMapping("/transaksjoner/person")
    @Operation(
        description = "Henter transaksjoner for person",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet transaksjoner for person"),
            ApiResponse(responseCode = "204", description = "Fant ingen data", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Feil i forespørselen", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåPerson(@RequestBody personRequest: PersonRequest): TransaksjonerDto {
        return reskontroLegacyService.hentTransaksjonerPåPerson(personRequest)
    }

    @GetMapping("/transaksjoner/transaksjonsid")
    @Operation(
        description = "Henter transaksjoner på transaksjonsid",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet transaksjoner på transaksjonsid"),
            ApiResponse(responseCode = "204", description = "Fant ingen data", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Feil i forespørselen", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
        ],
    )
    fun hentTransaksjonerPåTransaksjonsid(@RequestParam transaksjonsid: Long): TransaksjonerDto {
        return reskontroLegacyService.hentTransaksjonerPåTransaksjonsid(transaksjonsid)
    }

    @PostMapping("/innkrevingsinformasjon")
    @Operation(
        description = "Henter informasjon om innkrevingssaken knyttet til person",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet informasjon om innkrevingssaken knyttet til person"),
            ApiResponse(responseCode = "204", description = "Fant ingen data", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Feil i forespørselen", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
        ],
    )
    fun hentInformasjonOmInnkrevingssaken(@RequestBody personRequest: PersonRequest): InnkrevingssaksinformasjonDto {
        return reskontroLegacyService.hentInformasjonOmInnkrevingssaken(personRequest)
    }

    @PatchMapping("/endreRmForSak")
    @Operation(
        description = "Endrer rm for sak",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Endret rm for sak"),
            ApiResponse(responseCode = "400", description = "Feil i forespørselen", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Maskinporten-token er ikke gyldig", content = [Content()]),
        ],
    )
    fun endreRmForSak(@RequestBody endreRmForSak: EndreRmForSakRequest) {
        reskontroLegacyService.endreRmForSak(endreRmForSak.saksnummer, endreRmForSak.barn, endreRmForSak.nyttFødselsnummer)
    }
}
