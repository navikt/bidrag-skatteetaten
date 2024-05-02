package no.nav.bidrag.aktoerregister.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import no.nav.bidrag.aktoerregister.SECURE_LOGGER
import no.nav.bidrag.aktoerregister.consumer.PersonConsumer
import no.nav.bidrag.aktoerregister.consumer.SamhandlerConsumer
import no.nav.bidrag.aktoerregister.dto.AktoerDTO
import no.nav.bidrag.aktoerregister.dto.AktoerIdDTO
import no.nav.bidrag.aktoerregister.dto.enumer.Identtype
import no.nav.bidrag.aktoerregister.exception.AktørNotFoundException
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import no.nav.bidrag.aktoerregister.persistence.repository.AktørRepository
import no.nav.bidrag.aktoerregister.persistence.repository.HendelseRepository
import no.nav.bidrag.aktoerregister.persistence.repository.TidligereIdenterRepository
import no.nav.bidrag.domene.ident.Ident
import no.nav.bidrag.transport.samhandler.SamhandlerSøk
import no.nav.bidrag.transport.samhandler.SamhandlersøkeresultatDto
import org.springframework.core.convert.ConversionService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val LOGGER = KotlinLogging.logger {}

@Service
class AktørService(
    private val aktørRepository: AktørRepository,
    private val tidligereIdenterRepository: TidligereIdenterRepository,
    private val hendelseRepository: HendelseRepository,
    private val hendelseService: HendelseService,
    private val samhandlerConsumer: SamhandlerConsumer,
    private val personConsumer: PersonConsumer,
    private val conversionService: ConversionService,
    private val entityManager: EntityManager,
) {

    @Transactional
    fun hentAktoer(aktørId: AktoerIdDTO, tvingOppdatering: Boolean): AktoerDTO {
        val aktørIdent = Ident(aktørId.aktoerId)
        var aktør = hentAktørFraDatabase(aktørIdent)

        if (aktør != null && tvingOppdatering) {
            val hentetAktør = if (aktørId.identtype == Identtype.AKTOERNUMMER) hentAktørFraSamhandler(aktørIdent) else hentAktørFraPerson(aktørIdent)
            if (aktør != hentetAktør) {
                oppdaterAktør(aktør, hentetAktør, aktørId.aktoerId)
            }
        } else if (aktør == null) {
            aktør = hentNyAktør(aktørId, aktørIdent)
        }
        return conversionService.convert(aktør, AktoerDTO::class.java)
            ?: error("Konvertering av aktør til AktoerDTO feilet!")
    }

    private fun hentNyAktør(aktørId: AktoerIdDTO, aktørIdent: Ident) = if (aktørId.identtype == Identtype.AKTOERNUMMER) {
        hentAktørFraSamhandlerOgLagreTilDatabase(aktørIdent)
    } else {
        hentAktørFraPersonOgLagreTilDatabase(
            aktørIdent,
        )
    }

    private fun hentAktørFraSamhandlerOgLagreTilDatabase(aktørIdent: Ident): Aktør {
        LOGGER.debug { "Aktør ikke funnet i databasen. Henter aktør fra bidrag-samhandler" }
        hentAktørFraSamhandler(aktørIdent).let {
            lagreNyAktør(it)
            return it
        }
    }

    fun hentAktørFraSamhandler(aktørIdent: Ident): Aktør {
        val samhandler = samhandlerConsumer.hentSamhandler(aktørIdent)
            ?: throw AktørNotFoundException("Aktør ikke funnet i bidrag-samhandler.")
        return conversionService.convert(samhandler, Aktør::class.java)
            ?: error("Konvertering av samhandler til aktør for ident: $aktørIdent feilet!")
    }

    private fun hentAktørFraPersonOgLagreTilDatabase(personIdent: Ident): Aktør {
        LOGGER.debug { "Aktør ikke funnet i databasen. Henter aktør fra bidrag-person" }
        hentAktørFraPerson(personIdent).let {
            // Om det finnes tidligere identer må vi sjekke om disse eksisterer i databasen fra før av.
            // Om de gjør det skal vi oppdatere og ikke opprette ny ident.
            // Denne situasjonen kan oppstå om en endring av ident blir kalt via REST før vi har tatt imot hendelse fra PDL
            if (it.tidligereIdenter.isNotEmpty()) {
                var aktørFraDatabase: Aktør? = null
                it.tidligereIdenter.forEach {
                    aktørFraDatabase = hentAktørFraDatabase(Ident(it.tidligereAktoerIdent))
                    if (aktørFraDatabase != null) return@forEach
                }
                if (aktørFraDatabase != null) {
                    val originalIdent = aktørFraDatabase!!.aktørIdent
                    oppdaterAktør(aktørFraDatabase!!, it, originalIdent)
                    return aktørFraDatabase!!
                }
            }
            lagreNyAktør(it)
            return it
        }
    }

    fun hentAktørFraPerson(personIdent: Ident): Aktør {
        val person = personConsumer.hentPerson(personIdent)
            ?: throw AktørNotFoundException("Aktør ikke funnet i bidrag-person.")
        return conversionService.convert(person, Aktør::class.java)
            ?: error("Konvertering av person til aktør feilet!")
    }

    fun hentAktørFraDatabase(aktørIdent: Ident): Aktør? {
        return aktørRepository.findByAktørIdent(aktørIdent.verdi)
            ?: tidligereIdenterRepository.findByTidligereAktoerIdent(aktørIdent.verdi)?.aktør
    }

    fun oppdaterAktør(aktør: Aktør, nyAktør: Aktør, originalIdent: String?): String? {
        var slettetAktørIdent: String? = null
        try {
            // Denne kodesnutten går igjennom og sletter aktører som er duplikater. Dette har forekommet siden aktørregisteret
            // ikke har hatt ett forhold til tidligereIdenter fra starten av, noe som har ført til at aktører med ny ident har
            // blitt opprettet som en ny aktør.
            if (originalIdent != null && originalIdent != nyAktør.aktørIdent) {
                aktørRepository.findByAktørIdent(nyAktør.aktørIdent)?.let {
                    LOGGER.info { "Sletter aktør grunnet duplikat. Se secure logs." }
                    SECURE_LOGGER.info(
                        "Sletter aktør grunnet duplikat. " +
                            "Original ident: $originalIdent, ny aktør ident: ${nyAktør.aktørIdent}, gammel ident: ${it.aktørIdent}",
                    )
                    slettetAktørIdent = it.aktørIdent
                    it.hendelser.forEach { hendelse ->
                        hendelseRepository.delete(hendelse)
                    }
                    aktørRepository.delete(it)
                }
                entityManager.flush()
            }

            val oppdaterteFelterPåAktør = finnOppdaterteFelterPåAktør(aktør, nyAktør)

            aktør.tidligereIdenter.forEach {
                tidligereIdenterRepository.delete(it)
            }

            aktør.oppdaterAlleFelter(nyAktør)

            entityManager.flush()

            aktør.tidligereIdenter.forEach {
                it.aktør = aktør
            }
            aktør.dødsbo?.aktør = aktør
            hendelseService.opprettHendelserPåAktør(aktør, originalIdent, oppdaterteFelterPåAktør)
            SECURE_LOGGER.info("Lagrer aktør: ${aktør.aktørIdent}")
            aktørRepository.save(aktør)
            return slettetAktørIdent
        } catch (e: Exception) {
            SECURE_LOGGER.error("Ukjent feil for ident: ${aktør.aktørIdent}. Original ident: $originalIdent. \nFeil: ${e.message} ")
            throw e
        }
    }

    fun lagreNyAktør(aktør: Aktør) {
        try {
            aktørRepository.save(aktør)
            aktør.tidligereIdenter.forEach {
                it.aktør = aktør
            }
            aktør.dødsbo?.aktør = aktør
            hendelseService.opprettHendelserPåAktør(aktør, null, finnFelterPåNyAktør(aktør))
        } catch (e: DataIntegrityViolationException) {
            SECURE_LOGGER.error("DataIntegrityViolationException for ident: ${aktør.aktørIdent}. \nFeil: $e ")
            throw e
        }
    }

    @Transactional
    fun slettAktoer(aktoerIdDTO: AktoerIdDTO) {
        aktørRepository.deleteAktørByAktørIdent(aktoerIdDTO.aktoerId)
    }

    fun samhandlerSøk(samhandlerSøk: SamhandlerSøk): SamhandlersøkeresultatDto {
        return samhandlerConsumer.samhandlerSøk(samhandlerSøk) ?: throw AktørNotFoundException("Aktør ikke funnet i bidrag-samhandler under søk.")
    }

    private fun finnFelterPåNyAktør(aktør: Aktør): List<String> {
        val oppdaterteFelterPåAktør = mutableListOf<String>()

        oppdaterteFelterPåAktør.add("identOppdatering")

        if (aktør.fornavn != null) {
            oppdaterteFelterPåAktør.add("navnOppdatering")
        }
        if (aktør.norskKontonr != null ||
            aktør.iban != null ||
            aktør.swift != null ||
            aktør.bankNavn != null ||
            aktør.bankLandkode != null ||
            aktør.bankCode != null ||
            aktør.valutaKode != null
        ) {
            oppdaterteFelterPåAktør.add("kontonummerOppdatering")
        }
        if (aktør.adresselinje1 != null ||
            aktør.adresselinje2 != null ||
            aktør.adresselinje3 != null ||
            aktør.leilighetsnummer != null ||
            aktør.postnr != null ||
            aktør.poststed != null ||
            aktør.land != null
        ) {
            oppdaterteFelterPåAktør.add("adresseOppdatering")
        }
        if (aktør.fødtDato != null) {
            oppdaterteFelterPåAktør.add("fødtDatoOppdatering")
        }
        if (aktør.dødDato != null) {
            oppdaterteFelterPåAktør.add("dødDatoOppdatering")
        }
        if (aktør.gradering != null) {
            oppdaterteFelterPåAktør.add("graderingOppdatering")
        }
        if (aktør.dødsbo?.kontaktperson != null ||
            aktør.dødsbo?.adresselinje1 != null ||
            aktør.dødsbo?.adresselinje2 != null ||
            aktør.dødsbo?.adresselinje3 != null ||
            aktør.dødsbo?.leilighetsnummer != null ||
            aktør.dødsbo?.postnr != null ||
            aktør.dødsbo?.poststed != null ||
            aktør.dødsbo?.land != null
        ) {
            oppdaterteFelterPåAktør.add("dødsboOppdatering")
        }
        if (aktør.språkkode != null) {
            oppdaterteFelterPåAktør.add("språkOppdatering")
        }

        return oppdaterteFelterPåAktør
    }

    fun finnOppdaterteFelterPåAktør(aktør: Aktør, nyAktør: Aktør): List<String> {
        val oppdaterteFelterPåAktør = mutableListOf<String>()

        if (aktør.fornavn != nyAktør.fornavn ||
            aktør.etternavn != nyAktør.etternavn
        ) {
            oppdaterteFelterPåAktør.add("navnOppdatering")
        }
        if (aktør.aktørIdent != nyAktør.aktørIdent) {
            oppdaterteFelterPåAktør.add("identOppdatering")
        }
        if (aktør.norskKontonr != nyAktør.norskKontonr ||
            aktør.iban != nyAktør.iban ||
            aktør.swift != nyAktør.swift ||
            aktør.bankNavn != nyAktør.bankNavn ||
            aktør.bankLandkode != nyAktør.bankLandkode ||
            aktør.bankCode != nyAktør.bankCode ||
            aktør.valutaKode != nyAktør.valutaKode
        ) {
            oppdaterteFelterPåAktør.add("kontonummerOppdatering")
        }
        if (aktør.adresselinje1 != nyAktør.adresselinje1 ||
            aktør.adresselinje2 != nyAktør.adresselinje2 ||
            aktør.adresselinje3 != nyAktør.adresselinje3 ||
            aktør.leilighetsnummer != nyAktør.leilighetsnummer ||
            aktør.postnr != nyAktør.postnr ||
            aktør.poststed != nyAktør.poststed ||
            aktør.land != nyAktør.land
        ) {
            oppdaterteFelterPåAktør.add("adresseOppdatering")
        }
        if (aktør.fødtDato != nyAktør.fødtDato) {
            oppdaterteFelterPåAktør.add("fødtDatoOppdatering")
        }
        if (aktør.dødDato != nyAktør.dødDato) {
            oppdaterteFelterPåAktør.add("dødDatoOppdatering")
        }
        if (aktør.gradering != nyAktør.gradering) {
            oppdaterteFelterPåAktør.add("graderingOppdatering")
        }
        if (aktør.dødsbo?.kontaktperson != nyAktør.dødsbo?.kontaktperson ||
            aktør.dødsbo?.adresselinje1 != nyAktør.dødsbo?.adresselinje1 ||
            aktør.dødsbo?.adresselinje2 != nyAktør.dødsbo?.adresselinje2 ||
            aktør.dødsbo?.adresselinje3 != nyAktør.dødsbo?.adresselinje3 ||
            aktør.dødsbo?.leilighetsnummer != nyAktør.dødsbo?.leilighetsnummer ||
            aktør.dødsbo?.postnr != nyAktør.dødsbo?.postnr ||
            aktør.dødsbo?.poststed != nyAktør.dødsbo?.poststed ||
            aktør.dødsbo?.land != nyAktør.dødsbo?.land
        ) {
            oppdaterteFelterPåAktør.add("dødsboOppdatering")
        }
        if (aktør.språkkode != nyAktør.språkkode) {
            oppdaterteFelterPåAktør.add("språkOppdatering")
        }

        return oppdaterteFelterPåAktør
    }
}
