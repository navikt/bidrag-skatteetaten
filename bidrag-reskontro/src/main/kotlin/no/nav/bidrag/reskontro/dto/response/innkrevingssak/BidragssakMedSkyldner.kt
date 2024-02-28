package no.nav.bidrag.reskontro.dto.response.innkrevingssak

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "Bidragssak med skyldner",
    description = "Inneholder informasjon om bidragssaken fra skatt med skyldner",
)
data class BidragssakMedSkyldner(
    val skyldner: Skyldner,
    val bidragssak: Bidragssak,
)
