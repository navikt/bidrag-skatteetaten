package no.nav.bidrag.reskontro.controller

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.bidrag.commons.security.maskinporten.MaskinportenClient
import no.nav.bidrag.commons.tilgang.TilgangClient
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.reskontro.TestBidragReskontro
import no.nav.bidrag.reskontro.consumer.SkattReskontroConsumer
import no.nav.bidrag.reskontro.dto.consumer.Bidragssak
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerInput
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerOutput
import no.nav.bidrag.reskontro.dto.consumer.Retur
import no.nav.bidrag.reskontro.dto.consumer.Skyldner
import no.nav.bidrag.reskontro.dto.consumer.Transaksjon
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.reskontro.request.EndreRmForSakRequest
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [TestBidragReskontro::class, TilgangskontrollIT.Config::class])
@ActiveProfiles("test")
@EnableMockOAuth2Server
@AutoConfigureTestRestTemplate
class TilgangskontrollIT {

    @TestConfiguration
    class Config {
        @Bean
        fun maskinportenClient() = mockk<MaskinportenClient>(relaxed = true)

        @Bean @Primary
        fun tilgangClient() = mockk<TilgangClient>()

        @Bean
        fun skattReskontroConsumer() = mockk<SkattReskontroConsumer>()
    }

    @Autowired
    private lateinit var httpHeaderTestRestTemplate: HttpHeaderTestRestTemplate

    @Autowired
    private lateinit var tilgangClient: TilgangClient

    @Autowired
    private lateinit var skattReskontroConsumer: SkattReskontroConsumer

    @BeforeEach
    fun resetMocks() {
        clearMocks(tilgangClient, skattReskontroConsumer)
        every { tilgangClient.harTilgangSaksnummer(any()) } returns false
        every { tilgangClient.harTilgangPerson(any()) } returns false
    }

    @Test
    fun `POST innkrevningssak bidragssak returnerer 403 når tilgangskontroll nekter tilgang`() {
        val svar = httpHeaderTestRestTemplate.postForEntity<ProblemDetail>(
            "/innkrevningssak/bidragssak",
            SaksnummerRequest(Saksnummer("0000123")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        verify { tilgangClient.harTilgangSaksnummer(Saksnummer("0000123")) }
    }

    @Test
    fun `POST innkrevningssak bidragssak returnerer svar når tilgangskontroll gir tilgang`() {
        every { tilgangClient.harTilgangSaksnummer(Saksnummer("0000123")) } returns true
        every { skattReskontroConsumer.hentInnkrevningssakerPåSak(123L) } returns lagBidragssakOutput(123L)

        val svar = httpHeaderTestRestTemplate.postForEntity<String>(
            "/innkrevningssak/bidragssak",
            SaksnummerRequest(Saksnummer("0000123")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `POST innkrevningssak person returnerer 403 når tilgangskontroll nekter tilgang`() {
        val svar = httpHeaderTestRestTemplate.postForEntity<ProblemDetail>(
            "/innkrevningssak/person",
            PersonRequest(Personident("12345678910")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        verify { tilgangClient.harTilgangPerson(Personident("12345678910")) }
    }

    @Test
    fun `POST innkrevningssak person returnerer svar når tilgangskontroll gir tilgang`() {
        every { tilgangClient.harTilgangPerson(Personident("12345678910")) } returns true
        every { skattReskontroConsumer.hentInnkrevningssakerPåPerson(any()) } returns lagBidragssakOutputMedSkyldner()

        val svar = httpHeaderTestRestTemplate.postForEntity<String>(
            "/innkrevningssak/person",
            PersonRequest(Personident("12345678910")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `POST transaksjoner bidragssak returnerer 403 når tilgangskontroll nekter tilgang`() {
        val svar = httpHeaderTestRestTemplate.postForEntity<ProblemDetail>(
            "/transaksjoner/bidragssak",
            SaksnummerRequest(Saksnummer("0000123")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        verify { tilgangClient.harTilgangSaksnummer(Saksnummer("0000123")) }
    }

    @Test
    fun `POST transaksjoner bidragssak returnerer svar når tilgangskontroll gir tilgang`() {
        every { tilgangClient.harTilgangSaksnummer(Saksnummer("0000123")) } returns true
        every { skattReskontroConsumer.hentTransaksjonerPåBidragssak(123L) } returns lagTransaksjonerOutput()

        val svar = httpHeaderTestRestTemplate.postForEntity<String>(
            "/transaksjoner/bidragssak",
            SaksnummerRequest(Saksnummer("0000123")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `POST transaksjoner person returnerer 403 når tilgangskontroll nekter tilgang`() {
        val svar = httpHeaderTestRestTemplate.postForEntity<ProblemDetail>(
            "/transaksjoner/person",
            PersonRequest(Personident("12345678910")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        verify { tilgangClient.harTilgangPerson(Personident("12345678910")) }
    }

    @Test
    fun `POST transaksjoner person returnerer svar når tilgangskontroll gir tilgang`() {
        every { tilgangClient.harTilgangPerson(Personident("12345678910")) } returns true
        every { skattReskontroConsumer.hentTransaksjonerPåPerson(any()) } returns lagTransaksjonerOutput()

        val svar = httpHeaderTestRestTemplate.postForEntity<String>(
            "/transaksjoner/person",
            PersonRequest(Personident("12345678910")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `GET transaksjoner transaksjonsid returnerer 403 når tilgangskontroll nekter tilgang på saksnummer i svaret`() {
        every { skattReskontroConsumer.hentTransaksjonerPåTransaksjonsId(42L) } returns lagTransaksjonerOutput(saksnummer = 123L)

        val svar = httpHeaderTestRestTemplate.getForEntity<ProblemDetail>(
            "/transaksjoner/transaksjonsid?transaksjonsid=42",
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        verify { tilgangClient.harTilgangSaksnummer(Saksnummer("0000123")) }
    }

    @Test
    fun `GET transaksjoner transaksjonsid returnerer svar når tilgangskontroll gir tilgang`() {
        every { skattReskontroConsumer.hentTransaksjonerPåTransaksjonsId(42L) } returns lagTransaksjonerOutput(saksnummer = 123L)
        every { tilgangClient.harTilgangSaksnummer(Saksnummer("0000123")) } returns true

        val svar = httpHeaderTestRestTemplate.getForEntity<String>(
            "/transaksjoner/transaksjonsid?transaksjonsid=42",
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `POST innkrevingsinformasjon returnerer 403 når tilgangskontroll nekter tilgang`() {
        val svar = httpHeaderTestRestTemplate.postForEntity<ProblemDetail>(
            "/innkrevingsinformasjon",
            PersonRequest(Personident("12345678910")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        verify { tilgangClient.harTilgangPerson(Personident("12345678910")) }
    }

    @Test
    fun `POST innkrevigsinformasjon returnerer svar når tilgangskontroll gir tilgang`() {
        every { tilgangClient.harTilgangPerson(Personident("12345678910")) } returns true
        every { skattReskontroConsumer.hentInformasjonOmInnkrevingssaken(any()) } returns lagInnkrevingsinformasjonOutput()

        val svar = httpHeaderTestRestTemplate.postForEntity<String>(
            "/innkrevingsinformasjon",
            PersonRequest(Personident("12345678910")),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `PATCH endreRmForSak returnerer 403 når tilgangskontroll nekter tilgang`() {
        val svar = httpHeaderTestRestTemplate.patchForEntity<ProblemDetail>(
            "/endreRmForSak",
            EndreRmForSakRequest(
                saksnummer = Saksnummer("0000123"),
                barn = Personident("12345678910"),
                nyttFødselsnummer = Personident("10987654321"),
            ),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        verify { tilgangClient.harTilgangSaksnummer(Saksnummer("0000123")) }
    }

    @Test
    fun `PATCH endreRmForSak returnerer 200 når tilgangskontroll gir tilgang`() {
        every { tilgangClient.harTilgangSaksnummer(Saksnummer("0000123")) } returns true
        every { skattReskontroConsumer.endreRmForSak(any(), any(), any()) } returns lagEndreRmOutput(123L)

        val svar = httpHeaderTestRestTemplate.patchForEntity<String>(
            "/endreRmForSak",
            EndreRmForSakRequest(
                saksnummer = Saksnummer("0000123"),
                barn = Personident("12345678910"),
                nyttFødselsnummer = Personident("10987654321"),
            ),
        )

        assertThat(svar.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun lagBidragssakOutput(saksnummer: Long) = ReskontroConsumerOutput(
        innParametre = ReskontroConsumerInput(aksjonskode = 1, bidragssaksnummer = saksnummer),
        bidragssaker = listOf(
            Bidragssak(
                bidragssaksnummer = saksnummer,
                bmGjeldFastsettelsesgebyr = BigDecimal.ZERO,
                bmGjeldRest = BigDecimal.ZERO,
                bpGjeldFastsettelsesgebyr = BigDecimal.ZERO,
                perBarnISak = emptyList(),
            ),
        ),
        retur = Retur(kode = 0, beskrivelse = "OK"),
    )

    private fun lagBidragssakOutputMedSkyldner() = ReskontroConsumerOutput(
        innParametre = ReskontroConsumerInput(aksjonskode = 2, fodselsOrgnr = "12345678910"),
        skyldner = Skyldner(
            fodselsOrgnr = "12345678910",
            sumLopendeBidrag = BigDecimal.ZERO,
            innbetBelopUfordelt = BigDecimal.ZERO,
            gjeldIlagtGebyr = BigDecimal.ZERO,
        ),
        bidragssaker = listOf(
            Bidragssak(
                bidragssaksnummer = 123L,
                bmGjeldFastsettelsesgebyr = BigDecimal.ZERO,
                bmGjeldRest = BigDecimal.ZERO,
                bpGjeldFastsettelsesgebyr = BigDecimal.ZERO,
                perBarnISak = emptyList(),
            ),
        ),
        retur = Retur(kode = 0, beskrivelse = "OK"),
    )

    private fun lagTransaksjonerOutput(saksnummer: Long? = null) = ReskontroConsumerOutput(
        innParametre = ReskontroConsumerInput(aksjonskode = 3),
        transaksjoner = if (saksnummer != null) {
            listOf(Transaksjon(transaksjonsId = 42L, bidragssaksnummer = saksnummer, dato = "2024-01-01T00:00:00"))
        } else {
            emptyList()
        },
        retur = Retur(kode = 0, beskrivelse = "OK"),
    )

    private fun lagInnkrevingsinformasjonOutput() = ReskontroConsumerOutput(
        innParametre = ReskontroConsumerInput(aksjonskode = 6),
        skyldner = Skyldner(
            fodselsOrgnr = "12345678910",
            sumLopendeBidrag = BigDecimal.ZERO,
            innbetBelopUfordelt = BigDecimal.ZERO,
            gjeldIlagtGebyr = BigDecimal.ZERO,
        ),
        retur = Retur(kode = 0, beskrivelse = "OK"),
    )

    private fun lagEndreRmOutput(saksnummer: Long) = ReskontroConsumerOutput(
        innParametre = ReskontroConsumerInput(aksjonskode = 8, bidragssaksnummer = saksnummer),
        retur = Retur(kode = 0, beskrivelse = "OK"),
    )
}
