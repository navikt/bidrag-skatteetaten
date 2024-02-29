package no.nav.bidrag.aktoerregister.service

import no.nav.bidrag.aktoerregister.dto.AktoerIdDTO
import no.nav.bidrag.aktoerregister.dto.HendelseDTO
import no.nav.bidrag.aktoerregister.dto.enumer.Identtype
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import no.nav.bidrag.aktoerregister.persistence.entities.Hendelse
import no.nav.bidrag.aktoerregister.persistence.repository.HendelseRepository
import no.nav.bidrag.aktoerregister.persistence.repository.SekvensnummerOgIdent
import no.nav.bidrag.domene.ident.Ident
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class HendelseService(
    private val hendelseRepository: HendelseRepository,
) {

    fun hentHendelser(sekvensunummer: Int, antallHendelser: Int): List<HendelseDTO> {
        val hendelser =
            hendelseRepository.hentAlleHendelserMedSekvensnummerOgIdent(sekvensunummer, Pageable.ofSize(antallHendelser))

        return hendelser.distinctBy { it.aktoerIdent }
            .map {
                HendelseDTO(
                    sekvensnummer = it.sekvensnummer,
                    aktoerId = AktoerIdDTO(
                        aktoerId = it.aktoerIdent,
                        identtype = finnIdenttype(it),
                    ),
                )
            }
            .sortedBy { it.sekvensnummer }
    }

    private fun finnIdenttype(sekvensnummerOgIdent: SekvensnummerOgIdent): Identtype {
        return if (Ident(sekvensnummerOgIdent.aktoerIdent).erPersonIdent()) {
            Identtype.PERSONNUMMER
        } else {
            Identtype.AKTOERNUMMER
        }
    }

    fun opprettHendelserPåAktør(aktør: Aktør, originalIdent: String?, oppdaterteFelter: List<String> = listOf()) {
        if (originalIdent != null && originalIdent != aktør.aktørIdent) {
            val hendelse = Hendelse(
                aktørIdent = originalIdent,
                aktør = aktør,
                kontonummerOppdatering = oppdaterteFelter.contains("kontonummerOppdatering"),
                identOppdatering = oppdaterteFelter.contains("identOppdatering"),
                navnOppdatering = oppdaterteFelter.contains("navnOppdatering"),
                adresseOppdatering = oppdaterteFelter.contains("adresseOppdatering"),
                fødtDatoOppdatering = oppdaterteFelter.contains("fødtDatoOppdatering"),
                dødDatoOppdatering = oppdaterteFelter.contains("dødDatoOppdatering"),
                graderingOppdatering = oppdaterteFelter.contains("graderingOppdatering"),
                dødsboOppdatering = oppdaterteFelter.contains("dødsboOppdatering"),
                språkOppdatering = oppdaterteFelter.contains("språkOppdatering"),
            )
            aktør.addHendelse(hendelse)
        }
        aktør.addHendelse(
            Hendelse(
                aktørIdent = aktør.aktørIdent,
                aktør = aktør,
                kontonummerOppdatering = oppdaterteFelter.contains("kontonummerOppdatering"),
                identOppdatering = oppdaterteFelter.contains("identOppdatering"),
                navnOppdatering = oppdaterteFelter.contains("navnOppdatering"),
                adresseOppdatering = oppdaterteFelter.contains("adresseOppdatering"),
                fødtDatoOppdatering = oppdaterteFelter.contains("fødtDatoOppdatering"),
                dødDatoOppdatering = oppdaterteFelter.contains("dødDatoOppdatering"),
                graderingOppdatering = oppdaterteFelter.contains("graderingOppdatering"),
                dødsboOppdatering = oppdaterteFelter.contains("dødsboOppdatering"),
                språkOppdatering = oppdaterteFelter.contains("språkOppdatering"),
            ),
        )
    }
}
