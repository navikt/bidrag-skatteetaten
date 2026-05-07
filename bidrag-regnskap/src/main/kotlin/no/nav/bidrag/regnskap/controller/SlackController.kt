package no.nav.bidrag.regnskap.controller

import no.nav.bidrag.commons.service.slack.SlackService
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SlackController(private val slackService: SlackService) {

    @Protected
    @PostMapping("/slack")
    fun sendMelding(melding: String) {
        slackService.sendMelding(melding)
    }
}
