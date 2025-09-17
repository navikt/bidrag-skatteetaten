package no.nav.bidrag.regnskap.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.nav.bidrag.commons.security.maskinporten.MaskinportenClient
import no.nav.bidrag.transport.regnskap.krav.Kravliste
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.net.URI

class SkattConsumerTest {

    private lateinit var skattConsumer: SkattConsumer

    @MockK
    private lateinit var restTemplate: RestTemplate

    @MockK
    private lateinit var maskinportenClient: MaskinportenClient

    @MockK
    private lateinit var objectMapper: ObjectMapper

    private val skattUrl = "http://localhost:8080"
    private val scope = "test.scope"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        skattConsumer = SkattConsumer(skattUrl, scope, restTemplate, maskinportenClient, objectMapper)
    }

    @Test
    fun `sendKrav skal sende kravliste og returnere response`() {
        val kravliste = Kravliste(listOf())
        val expectedUri = URI.create("$skattUrl${SkattConsumer.KRAV_PATH}")
        val expectedResponse = ResponseEntity("Success", HttpStatus.OK)

        every { maskinportenClient.hentMaskinportenToken(any()) } returns mockk {
            every { parsedString } returns "mockedToken"
        }
        every { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(any()) } returns "{}"
        every {
            restTemplate.exchange(
                expectedUri,
                HttpMethod.POST,
                any(),
                String::class.java,
            )
        } returns expectedResponse

        val response = skattConsumer.sendKrav(kravliste)

        assertEquals(expectedResponse, response)
        verify {
            restTemplate.exchange(
                expectedUri,
                HttpMethod.POST,
                match {
                    it.body == kravliste && (it.headers[HttpHeaders.AUTHORIZATION]?.get(0) == "Bearer mockedToken")
                },
                String::class.java,
            )
        }
    }

    @Test
    fun `sendKrav skal kaste exception ved HTTP error`() {
        val kravliste = Kravliste(listOf())
        val forventetUrl = URI.create("$skattUrl${SkattConsumer.KRAV_PATH}")

        every { maskinportenClient.hentMaskinportenToken(any()) } returns mockk {
            every { parsedString } returns "mockedToken"
        }
        every { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(any()) } returns "{}"
        every {
            restTemplate.exchange(
                forventetUrl,
                HttpMethod.POST,
                any(),
                String::class.java,
            )
        } throws mockk<HttpStatusCodeException> {
            every { statusCode } returns HttpStatus.INTERNAL_SERVER_ERROR
        }

        val exception = assertThrows<HttpStatusCodeException> { skattConsumer.sendKrav(kravliste) }

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.statusCode)
        verify {
            restTemplate.exchange(
                forventetUrl,
                HttpMethod.POST,
                match {
                    it.body == kravliste && (it.headers[HttpHeaders.AUTHORIZATION]?.get(0) == "Bearer mockedToken")
                },
                String::class.java,
            )
        }
    }
}
