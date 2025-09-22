package no.nav.bidrag.regnskap.consumer

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.bidrag.commons.util.PersonidentGenerator
import wiremock.com.google.common.net.HttpHeaders

class ReskontroApiWireMock {

    companion object {
        private const val PORT = 8101
    }

    private val mock = WireMockServer(PORT)

    init {
        mock.start()
    }

    internal fun reskontroIngenResponse() {
        mock.stubFor(
            WireMock.post(WireMock.anyUrl()).willReturn(
                WireMock.aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json").withStatus(200).withBody(
                    """
             "transaksjoner": []
        """,
                ),
            ),
        )
    }
}
