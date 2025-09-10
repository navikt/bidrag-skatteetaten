package no.nav.bidrag.regnskap.service

import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.regnskap.SECURE_LOGGER
import no.nav.bidrag.regnskap.consumer.BidragPersonConsumer
import no.nav.bidrag.regnskap.persistence.entity.Oppdrag
import no.nav.bidrag.transport.person.hendelse.Endringsmelding
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HendelseService(
    private val persistenceService: PersistenceService,
    private val bidragPersonConsumer: BidragPersonConsumer,
) {

    @Transactional
    fun behandlePersonhendelse(endringsmelding: Endringsmelding) {
        for (endring in endringsmelding.endringer) {
            if (endring.identendring != null && endring.identendring!!.identifikasjonsnummer != null) {
                val person = bidragPersonConsumer.hentPerson(Ident(endring.identendring!!.identifikasjonsnummer!!))!!.person.ident

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
    }

    fun logIdentendring(
        gammelIdent: String,
        nyPersonident: Personident,
        oppdrag: Oppdrag,
    ) {
        SECURE_LOGGER.info("Personhendelse: $gammelIdent oppdatert til $nyPersonident for oppdragId: ${oppdrag.oppdragId}")
    }
}
