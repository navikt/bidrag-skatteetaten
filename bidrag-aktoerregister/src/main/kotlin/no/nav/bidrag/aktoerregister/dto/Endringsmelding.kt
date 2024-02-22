package no.nav.bidrag.aktoerregister.dto

data class Endringsmelding(
    val aktørid: String,
    val personidenter: Set<String>,
)
