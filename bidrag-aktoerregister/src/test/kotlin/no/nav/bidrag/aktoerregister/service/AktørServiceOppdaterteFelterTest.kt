package no.nav.bidrag.aktoerregister.service

import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.persistence.EntityManager
import no.nav.bidrag.aktoerregister.consumer.PersonConsumer
import no.nav.bidrag.aktoerregister.consumer.SamhandlerConsumer
import no.nav.bidrag.aktoerregister.dto.enumer.Identtype
import no.nav.bidrag.aktoerregister.persistence.entities.Aktør
import no.nav.bidrag.aktoerregister.persistence.entities.Dødsbo
import no.nav.bidrag.aktoerregister.persistence.repository.AktørRepository
import no.nav.bidrag.aktoerregister.persistence.repository.HendelseRepository
import no.nav.bidrag.aktoerregister.persistence.repository.TidligereIdenterRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.convert.ConversionService
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class AktørServiceOppdaterteFelterTest {

    @MockK(relaxed = true)
    private lateinit var aktørRepository: AktørRepository

    @MockK(relaxed = true)
    private lateinit var tidligereIdenterRepository: TidligereIdenterRepository

    @MockK(relaxed = true)
    private lateinit var hendelseRepository: HendelseRepository

    @MockK(relaxed = true)
    private lateinit var hendelseService: HendelseService

    @MockK(relaxed = true)
    private lateinit var samhandlerConsumer: SamhandlerConsumer

    @MockK(relaxed = true)
    private lateinit var personConsumer: PersonConsumer

    @MockK(relaxed = true)
    private lateinit var conversionService: ConversionService

    @MockK(relaxed = true)
    private lateinit var entityManager: EntityManager

    @InjectMockKs
    private lateinit var aktørService: AktørService

    @Test
    fun `skal ikke sette noe om alt er likt`() {
        val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name)
        val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name)

        val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

        oppdaterteFelter shouldHaveSize 0
    }

    @Nested
    inner class Kontonummer {

        @Test
        fun `skal sette kontonummerOppdatering norskKontonr`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, norskKontonr = null)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, norskKontonr = "12345")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("kontonummerOppdatering")
        }

        @Test
        fun `skal sette kontonummerOppdatering iban`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, iban = null)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, iban = "12345")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("kontonummerOppdatering")
        }

        @Test
        fun `skal sette kontonummerOppdatering swift`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, swift = null)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, swift = "12345")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("kontonummerOppdatering")
        }

        @Test
        fun `skal sette kontonummerOppdatering bankNavn`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, bankNavn = null)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, bankNavn = "12345")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("kontonummerOppdatering")
        }

        @Test
        fun `skal sette kontonummerOppdatering bankLandkode`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, bankLandkode = null)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, bankLandkode = "12345")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("kontonummerOppdatering")
        }

        @Test
        fun `skal sette kontonummerOppdatering bankCode`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, bankCode = null)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, bankCode = "12345")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("kontonummerOppdatering")
        }

        @Test
        fun `skal sette kontonummerOppdatering valutaKode`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, valutaKode = null)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, bankCode = "12345")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("kontonummerOppdatering")
        }
    }

    @Nested
    inner class Ident {

        @Test
        fun `skal sette identOppdatering valutaKode`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name)
            val nyAktør = Aktør(aktørIdent = "1234", aktørType = Identtype.PERSONNUMMER.name)

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("identOppdatering")
        }
    }

    @Nested
    inner class Navn {

        @Test
        fun `skal sette identOppdatering fornavn`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, fornavn = "Per")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, fornavn = "Emil")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("navnOppdatering")
        }

        @Test
        fun `skal sette identOppdatering etternavn`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, etternavn = "Persen")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, etternavn = "Emilsen")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("navnOppdatering")
        }
    }

    @Nested
    inner class Adresse {

        @Test
        fun `skal sette adresseOppdatering adresselinje1`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, adresselinje1 = "Adress")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, adresselinje1 = "Adressen")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("adresseOppdatering")
        }

        @Test
        fun `skal sette adresseOppdatering adresselinje2`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, adresselinje2 = "Persen")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, adresselinje2 = "Emilsen")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("adresseOppdatering")
        }

        @Test
        fun `skal sette adresseOppdatering adresselinje3`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, adresselinje3 = "Persen")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, adresselinje3 = "Emilsen")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("adresseOppdatering")
        }

        @Test
        fun `skal sette adresseOppdatering leilighetsnummer`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, leilighetsnummer = "Persen")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, leilighetsnummer = "Emilsen")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("adresseOppdatering")
        }

        @Test
        fun `skal sette adresseOppdatering postnr`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, postnr = "Persen")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, postnr = "Emilsen")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("adresseOppdatering")
        }

        @Test
        fun `skal sette adresseOppdatering poststed`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, poststed = "Persen")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, poststed = "Emilsen")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("adresseOppdatering")
        }

        @Test
        fun `skal sette adresseOppdatering land`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, land = "Persen")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, land = "Emilsen")

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("adresseOppdatering")
        }
    }

    @Nested
    inner class Dato {

        @Test
        fun `skal sette fødtDatoOppdatering`() {
            val fødtDato = LocalDate.now()
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, fødtDato = fødtDato)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, fødtDato = fødtDato.minusMonths(1))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("fødtDatoOppdatering")
        }

        @Test
        fun `skal sette dødDatoOppdatering`() {
            val dødDato = LocalDate.now()
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødDato = dødDato)
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødDato = dødDato.minusMonths(1))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødDatoOppdatering")
        }
    }

    @Nested
    inner class Gradering {

        @Test
        fun `skal sette graderingOppdatering`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, gradering = "Super streng")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, gradering = null)

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("graderingOppdatering")
        }
    }

    @Nested
    inner class Språk {

        @Test
        fun `skal sette språkOppdatering`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, språkkode = "NB")
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, språkkode = null)

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("språkOppdatering")
        }
    }

    @Nested
    inner class DødsboOppdatering {

        @Test
        fun `skal sette dødsboOppdatering kontaktperson`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(kontaktperson = "Person"))
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(kontaktperson = "Person2"))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødsboOppdatering")
        }

        @Test
        fun `skal sette dødsboOppdatering adresselinje1`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(adresselinje1 = "Person"))
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(adresselinje1 = "Person2"))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødsboOppdatering")
        }

        @Test
        fun `skal sette dødsboOppdatering adresselinje2`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(adresselinje2 = "Person"))
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(adresselinje2 = "Person2"))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødsboOppdatering")
        }

        @Test
        fun `skal sette dødsboOppdatering adresselinje3`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(adresselinje3 = "Person"))
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(adresselinje3 = "Person2"))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødsboOppdatering")
        }

        @Test
        fun `skal sette dødsboOppdatering leilighetsnummer`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(leilighetsnummer = "Person"))
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(leilighetsnummer = "Person2"))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødsboOppdatering")
        }

        @Test
        fun `skal sette dødsboOppdatering postnr`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(postnr = "Person"))
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(postnr = "Person2"))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødsboOppdatering")
        }

        @Test
        fun `skal sette dødsboOppdatering poststed`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(poststed = "Person"))
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(poststed = "Person2"))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødsboOppdatering")
        }

        @Test
        fun `skal sette dødsboOppdatering land`() {
            val aktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(land = "Person"))
            val nyAktør = Aktør(aktørIdent = "123", aktørType = Identtype.PERSONNUMMER.name, dødsbo = Dødsbo(land = "Person2"))

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldContainOnly listOf("dødsboOppdatering")
        }

        @Test
        fun `skal sette dødsboOppdatering ingen endring`() {
            val aktør =
                Aktør(
                    aktørIdent = "123",
                    aktørType = Identtype.PERSONNUMMER.name,
                    dødsbo = Dødsbo(kontaktperson = "Person", adresselinje1 = "Adresse", poststed = "Poststed", postnr = "Postnr", land = "Person"),
                )
            val nyAktør =
                Aktør(
                    aktørIdent = "123",
                    aktørType = Identtype.PERSONNUMMER.name,
                    dødsbo = Dødsbo(kontaktperson = "Person", adresselinje1 = "Adresse", poststed = "Poststed", postnr = "Postnr", land = "Person"),
                )

            val oppdaterteFelter = aktørService.finnOppdaterteFelterPåAktør(aktør, nyAktør)

            oppdaterteFelter shouldHaveSize 0
        }
    }
}
