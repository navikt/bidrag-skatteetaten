package no.nav.bidrag.reskontro.dto.response.innkrevingssaksinformasjon

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "Innkrevingssaksinformasjon",
    description = "Inneholder informasjon om innkrevingssaken.",
)
data class Innkrevingssaksinformasjon(
    val skyldnerinformasjon: Skyldnerinformasjon,
    val gjeldendeBetalingsordning: GjeldendeBetalingsordning,
    val nyBetalingsordning: NyBetalingsordning,
    val innkrevingssakshistorikk: List<Innkrevingssakshistorikk>,
)
