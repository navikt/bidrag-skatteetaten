package no.nav.bidrag.reskontro.dto.response.transaksjoner

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "Transaksjoner",
)
data class Transaksjoner(
    @field:Schema(
        description = "Liste over alle transaksjoenen på bidragssak",
    )
    val transaksjoner: List<Transaksjon>,
)
