package no.nav.bidrag.reskontro.dto.response.innkrevingssak

import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer

data class EndreRmForSak(
    val saksnummer: Saksnummer,
    val barn: Personident,
    val nyttFødselsnummer: Personident,
)
