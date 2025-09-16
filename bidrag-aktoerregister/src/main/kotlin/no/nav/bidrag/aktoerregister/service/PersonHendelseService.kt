package no.nav.bidrag.aktoerregister.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import no.nav.bidrag.aktoerregister.exception.AktørNotFoundException
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.transport.person.hendelse.Endringsmelding
import org.springframework.stereotype.Service

private val LOGGER = KotlinLogging.logger { }

@Service
class PersonHendelseService(private val objectMapper: ObjectMapper, private val aktørService: AktørService) {

    @Transactional
    fun behandleHendelse(hendelse: String) {
        LOGGER.info { "Behandler hendelse: $hendelse" }
        val endringsmelding = mapEndringsmelding(hendelse)

        endringsmelding.personidenter.forEach { ident ->
            val aktør = aktørService.hentAktørFraDatabase(Ident(ident))
            aktør?.let {
                LOGGER.info { "Fant lagret aktør $it." }
                try {
                    val aktørFraPerson = aktørService.hentAktørFraPerson(Ident(ident))
                    if (aktør != aktørFraPerson) {
                        LOGGER.info { "Lagret aktør $it er ulik ny aktør fra hendelse. Oppdaterer med nye verdier." }
                        aktørService.oppdaterAktør(aktør, aktørFraPerson, ident)
                    } else {
                        LOGGER.info { "Lagret aktør $it er ikke ulik ny aktør fra hendelse. Går til neste hendelse." }
                    }
                    return
                } catch (e: AktørNotFoundException) {
                    LOGGER.error(e) { "Aktør ikke funnet i bidrag-person! Fant ikke person for hendelse: $hendelse" }
                }
            }
        }
    }

    private fun mapEndringsmelding(hendelse: String): Endringsmelding = objectMapper.readValue(hendelse, Endringsmelding::class.java)
}
