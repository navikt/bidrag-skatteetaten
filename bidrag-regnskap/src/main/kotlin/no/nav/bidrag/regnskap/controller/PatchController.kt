package no.nav.bidrag.regnskap.controller

import no.nav.bidrag.regnskap.dto.patch.OppdaterReferanseRequest
import no.nav.bidrag.regnskap.dto.patch.PatchMottakerRequest
import no.nav.bidrag.regnskap.dto.patch.ReferanseForVedtakResponse
import no.nav.bidrag.regnskap.service.OppdragService
import no.nav.bidrag.regnskap.service.PatchService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class PatchController(
    private val oppdragService: OppdragService,
    private val patchService: PatchService,
) {

    @PostMapping("/patchMottaker")
    fun patchMottaker(@RequestBody patchMottakerRequest: PatchMottakerRequest) {
        oppdragService.patchMottaker(patchMottakerRequest.saksnummer, patchMottakerRequest.kravhaver, patchMottakerRequest.mottaker)
    }

    @GetMapping("/hentReferanseForVedtak")
    fun hentReferanseForVedtak(@RequestParam vedtakId: Int): ResponseEntity<List<ReferanseForVedtakResponse>> {
        val referanseForVedtak = patchService.hentReferanseForVedtak(vedtakId)
        return ResponseEntity.ok(referanseForVedtak)
    }

    @GetMapping("/hentAlleVedtakMedTommeReferanser")
    fun hentAlleVedtakMedTommeReferanser(): ResponseEntity<List<Int>> {
        val tommeRefernaser = patchService.hentAlleTommeReferanser()
        return ResponseEntity.ok(tommeRefernaser)
    }

    @PostMapping("/patchReferanseForOppdragsperiode")
    fun patchReferanseForOppdragsperiode(@RequestBody oppdaterReferanseRequest: OppdaterReferanseRequest) {
        patchService.oppdaterReferanseForOppdragsperiode(oppdaterReferanseRequest)
    }
}
