package no.nav.bidrag.regnskap.service

import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.regnskap.dto.patch.OppdaterReferanseRequest
import no.nav.bidrag.regnskap.dto.patch.ReferanseForVedtakResponse
import no.nav.bidrag.regnskap.persistence.entity.Oppdragsperiode
import no.nav.bidrag.regnskap.persistence.repository.OppdragRepository
import no.nav.bidrag.regnskap.persistence.repository.OppdragsperiodeRepository
import org.springframework.stereotype.Service

@Service
class PatchService(
    private val oppdragsperiodeRepository: OppdragsperiodeRepository,
    private val oppdragRepository: OppdragRepository,
) {

    fun hentReferanseForVedtak(vedtakId: Int): List<ReferanseForVedtakResponse> = oppdragsperiodeRepository.findAllByVedtakIdAndReferanseIsNotNull(vedtakId).map { oppdragsperiode ->
        opprettReferanseForVedtakResponse(oppdragsperiode)
    }

    fun hentReferanseForSak(saksnummer: String): List<ReferanseForVedtakResponse> = oppdragRepository.findAllBySakId(saksnummer)
        .flatMap { it.oppdragsperioder }
        .filter { oppdragsperiode -> oppdragsperiode.referanse != null }
        .map { oppdragsperiode ->
            opprettReferanseForVedtakResponse(oppdragsperiode)
        }

    fun hentAlleTommeReferanser(): List<Int> = oppdragsperiodeRepository.findAllByReferanse("").map { it.vedtakId }

    fun oppdaterReferanseForOppdragsperiode(oppdaterReferanseRequest: OppdaterReferanseRequest) {
        val oppdragsperiode = oppdragsperiodeRepository.findById(oppdaterReferanseRequest.oppdragsperiodeId).orElseThrow { IllegalArgumentException("Oppdragsperiode med id ${oppdaterReferanseRequest.oppdragsperiodeId} finnes ikke") }
        oppdragsperiode.referanse = oppdaterReferanseRequest.referanse
        oppdragsperiodeRepository.save(oppdragsperiode)
    }

    private fun opprettReferanseForVedtakResponse(oppdragsperiode: Oppdragsperiode): ReferanseForVedtakResponse = ReferanseForVedtakResponse(
        referanse = oppdragsperiode.referanse,
        vedtakId = oppdragsperiode.vedtakId,
        oppdragId = oppdragsperiode.oppdrag!!.oppdragId!!,
        oppdragsperiodeId = oppdragsperiode.oppdragsperiodeId!!,
        sakId = oppdragsperiode.oppdrag.sakId,
        stønadstype = oppdragsperiode.oppdrag.stønadType,
        vedtakstype = Vedtakstype.valueOf(oppdragsperiode.vedtakType),
    )
}
