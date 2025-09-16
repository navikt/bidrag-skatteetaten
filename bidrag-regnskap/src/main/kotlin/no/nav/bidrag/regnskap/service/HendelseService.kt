package no.nav.bidrag.regnskap.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.regnskap.consumer.BidragPersonConsumer
import no.nav.bidrag.regnskap.persistence.entity.Oppdrag
import no.nav.bidrag.transport.person.hendelse.Endringsmelding
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val LOGGER = KotlinLogging.logger {}

@Service
class HendelseService(
    private val persistenceService: PersistenceService,
    private val bidragPersonConsumer: BidragPersonConsumer,
) {

    @Transactional
    fun behandlePersonhendelse(endringsmelding: Endringsmelding) {
        endringsmelding.endringer.forEach { endring ->
            val identifikasjonsnummer = endring.identendring?.identifikasjonsnummer
            if (identifikasjonsnummer == null) {
                LOGGER.warn { "Mangler identifikasjonsnummer i endring fra personhendelse. Endring: $endring" }
                return@forEach
            }
            val person = bidragPersonConsumer.hentPerson(Ident(identifikasjonsnummer))?.person?.ident

            if (person == null) {
                LOGGER.warn { "Person med ident $identifikasjonsnummer ble ikke funnet i bidrag-person" }
                return@forEach
            }

            endringsmelding.personidenter.forEach {
                persistenceService.hentAlleMottakereMedIdent(it)
                    .filter { mottaker -> mottaker.mottakerIdent != person.verdi }
                    .forEach { mottaker ->
                        mottaker.mottakerIdent = person.verdi
                        logIdentendring(it, person, mottaker)
                    }

                persistenceService.hentAlleKravhavereMedIdent(it)
                    .filter { kravhaver -> kravhaver.kravhaverIdent != person.verdi }
                    .forEach { kravhaver ->
                        kravhaver.kravhaverIdent = person.verdi
                        logIdentendring(it, person, kravhaver)
                    }

                persistenceService.hentAlleSkyldnereMedIdent(it)
                    .filter { skyldner -> skyldner.skyldnerIdent != person.verdi }
                    .forEach { skyldner ->
                        skyldner.skyldnerIdent = person.verdi
                        logIdentendring(it, person, skyldner)
                    }

                persistenceService.hentAlleGjelderMedIdent(it)
                    .filter { gjelder -> gjelder.gjelderIdent != person.verdi }
                    .forEach { gjelder ->
                        gjelder.gjelderIdent = person.verdi
                        logIdentendring(it, person, gjelder)
                    }
            }
        }
    }

    fun logIdentendring(
        gammelIdent: String,
        nyPersonident: Personident,
        oppdrag: Oppdrag,
    ) {
        LOGGER.debug { "Personhendelse: $gammelIdent oppdatert til $nyPersonident for oppdragId: ${oppdrag.oppdragId}" }
    }
}
