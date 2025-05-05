package no.nav.bidrag.regnskap.service

import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.regnskap.dto.patch.OppdaterReferanseRequest
import no.nav.bidrag.regnskap.dto.patch.ReferanseForVedtakResponse
import no.nav.bidrag.regnskap.persistence.repository.OppdragsperiodeRepository
import org.springframework.stereotype.Service

@Service
class PatchService(
    private val oppdragsperiodeRepository: OppdragsperiodeRepository,
) {

    fun hentReferanseForVedtak(vedtakId: Int): List<ReferanseForVedtakResponse> = oppdragsperiodeRepository.findAllByVedtakIdAndReferanseIsNotNull(vedtakId).map { oppdragsperiode ->
        ReferanseForVedtakResponse(
            referanse = oppdragsperiode.referanse,
            vedtakId = oppdragsperiode.vedtakId,
            oppdragId = oppdragsperiode.oppdrag!!.oppdragId!!,
            oppdragsperiodeId = oppdragsperiode.oppdragsperiodeId!!,
            sakId = oppdragsperiode.oppdrag.sakId,
            stønadstype = oppdragsperiode.oppdrag.stønadType,
            vedtakstype = Vedtakstype.valueOf(oppdragsperiode.vedtakType),
        )
    }

    fun hentAlleTommeReferanser(): List<Int> = oppdragsperiodeRepository.findAllByReferanseIsEmpty().map { it.vedtakId }

    fun oppdaterReferanseForOppdragsperiode(oppdaterReferanseRequest: OppdaterReferanseRequest) {
        val oppdragsperiode = oppdragsperiodeRepository.findById(oppdaterReferanseRequest.oppdragsperiodeId).orElseThrow { IllegalArgumentException("Oppdragsperiode med id ${oppdaterReferanseRequest.oppdragsperiodeId} finnes ikke") }
        oppdragsperiode.referanse = oppdaterReferanseRequest.referanse
        oppdragsperiodeRepository.save(oppdragsperiode)
    }
}
