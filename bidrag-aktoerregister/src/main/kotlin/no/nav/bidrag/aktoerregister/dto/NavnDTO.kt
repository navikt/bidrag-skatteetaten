package no.nav.bidrag.aktoerregister.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Representerer navn for en bidragsaktør.")
data class NavnDTO(
    @Schema(description = "Personens fornavn og eventuelle mellomnavn. Benyttes ikke for samhandlere.")
    val fornavn: String? = null,

    @Schema(description = "Personens etternavn eller samhandlerens fulle navn.")
    val etternavn: String? = null,
)
