package no.nav.bidrag.regnskap.dto.patch

import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype

data class ReferanseForVedtakRequest(
    val vedtakId: Int,
)

data class ReferanseForVedtakResponse(
    val referanse: String?,
    val vedtakId: Int,
    val oppdragId: Int,
    val oppdragsperiodeId: Int,
    val sakId: String,
    val stønadstype: Stønadstype,
    val vedtakstype: Vedtakstype,
)

data class OppdaterReferanseRequest(
    val referanse: String,
    val oppdragsperiodeId: Int,
)
