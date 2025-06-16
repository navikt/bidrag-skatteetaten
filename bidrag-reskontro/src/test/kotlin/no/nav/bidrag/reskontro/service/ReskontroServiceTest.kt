package no.nav.bidrag.reskontro.service

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.commons.util.PersonidentGenerator
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.reskontro.consumer.SkattReskontroConsumer
import no.nav.bidrag.reskontro.dto.consumer.BarnISak
import no.nav.bidrag.reskontro.dto.consumer.Bidragssak
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerInput
import no.nav.bidrag.reskontro.dto.consumer.ReskontroConsumerOutput
import no.nav.bidrag.reskontro.dto.consumer.Retur
import no.nav.bidrag.transport.reskontro.request.SaksnummerRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(
    MockKExtension::class,
)
class ReskontroServiceTest {
    @MockK
    private lateinit var skattReskontroConsumer: SkattReskontroConsumer

    @InjectMockKs
    private lateinit var reskontroService: ReskontroService

    @Test
    fun `skal hente innkrevingssak på saksnummer`() {
        val saksnummer =
            123L
        val bmGjeldGebyr =
            BigDecimal(
                1200,
            )
        val bmGjeldRest =
            BigDecimal(
                700,
            )
        val bpGjeldGebyr =
            BigDecimal(
                1200,
            )
        val restGjeldOffentlig =
            BigDecimal(
                0,
            )
        val restGjeldPrivat =
            BigDecimal(
                10000,
            )
        val sumIkkeUtbetalt =
            BigDecimal(
                10000,
            )
        val sumForskuddUtbetalt =
            BigDecimal(
                10000,
            )
        val periodeSisteDatoFom =
            "2023-01-01T00:00:00.00000"
        val periodeSisteDatoTom =
            "2023-03-31T00:00:00.00000"
        val erStoppIUtbetaling =
            "N"

        val barnISak =
            BarnISak(
                fodselsnummer = PersonidentGenerator.genererFødselsnummer(),
                restGjeldOffentlig = restGjeldOffentlig,
                restGjeldPrivat = restGjeldPrivat,
                sumIkkeUtbetalt = sumIkkeUtbetalt,
                sumForskuddUtbetalt = sumForskuddUtbetalt,
                periodeSisteDatoFom = periodeSisteDatoFom,
                periodeSisteDatoTom = periodeSisteDatoTom,
                stoppUtbetaling = erStoppIUtbetaling,
            )
        val saksnummerRequest =
            SaksnummerRequest(
                Saksnummer(
                    saksnummer.toString(),
                ),
            )
        val innkrevingssaksResponse =
            ReskontroConsumerOutput(
                innParametre =
                ReskontroConsumerInput(
                    1,
                    saksnummer,
                ),
                bidragssaker =
                listOf(
                    Bidragssak(
                        bidragssaksnummer = saksnummer,
                        bmGjeldFastsettelsesgebyr = bmGjeldGebyr,
                        bmGjeldRest = bmGjeldRest,
                        bpGjeldFastsettelsesgebyr = bpGjeldGebyr,
                        perBarnISak =
                        listOf(
                            barnISak,
                        ),
                    ),
                ),
                retur =
                Retur(
                    0,
                ),
            )

        every {
            skattReskontroConsumer.hentInnkrevningssakerPåSak(
                saksnummer,
            )
        } returns innkrevingssaksResponse

        val bidragssak =
            reskontroService.hentInnkrevingssakPåSak(
                saksnummerRequest,
            )!!

        bidragssak.saksnummer?.verdi shouldBe saksnummer.toString()
        bidragssak.bmGjeldRest shouldBe bmGjeldRest
        bidragssak.bmGjeldFastsettelsesgebyr shouldBe bmGjeldGebyr
        bidragssak.bpGjeldFastsettelsesgebyr shouldBe bpGjeldGebyr
        bidragssak.barn shouldHaveSize 1
        bidragssak.barn.first().personident?.verdi shouldBe barnISak.fodselsnummer
        bidragssak.barn.first().erStoppIUtbetaling shouldBe false
        bidragssak.barn.first().restGjeldPrivat shouldBe restGjeldPrivat
        bidragssak.barn.first().restGjeldOffentlig shouldBe restGjeldOffentlig
        bidragssak.barn.first().sumForskuddUtbetalt shouldBe sumForskuddUtbetalt
        bidragssak.barn.first().sumIkkeUtbetalt shouldBe sumIkkeUtbetalt
        bidragssak.barn.first().periode?.fom shouldBe
            LocalDateTime.parse(
                periodeSisteDatoFom,
            )
                .toLocalDate()
        bidragssak.barn.first().periode?.til shouldBe
            LocalDateTime.parse(
                periodeSisteDatoTom,
            )
                .toLocalDate()
                .plusDays(
                    1,
                )
    }
}
