package no.nav.bidrag.regnskap.controller

import no.nav.bidrag.regnskap.dto.patch.PatchMottakerRequest
import no.nav.bidrag.regnskap.service.OppdragService
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Protected
@RestController
class PatchController(private val oppdragService: OppdragService) {

    @PostMapping("/patchMottaker")
    fun patchMottaker(@RequestBody patchMottakerRequest: PatchMottakerRequest) {
        oppdragService.patchMottaker(patchMottakerRequest.saksnummer, patchMottakerRequest.kravhaver, patchMottakerRequest.mottaker)
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
}
