package no.nav.bidrag.reskontrolegacy.reskws

import no.nav.bidrag.reskontrolegacy.generated.CResknObjectHolder
import no.nav.bidrag.reskontrolegacy.generated.RohInnkrevInfo
import no.nav.bidrag.reskontrolegacy.generated.RohInnkrevInfoResponse
import no.nav.bidrag.reskontrolegacy.generated.RohPrPersPrSakPrBarn
import no.nav.bidrag.reskontrolegacy.generated.RohPrPersPrSakPrBarnResponse
import no.nav.bidrag.reskontrolegacy.generated.RohPrSakPrBarn
import no.nav.bidrag.reskontrolegacy.generated.RohRMEndreFNR
import no.nav.bidrag.reskontrolegacy.generated.RohRMEndreFNRResponse
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrPersPrOrg
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrPersPrOrgResponse
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrSak
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrSakResponse
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrTransID
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrTransIDResponse
import org.springframework.ws.client.core.support.WebServiceGatewaySupport
import org.springframework.ws.soap.client.core.SoapActionCallback
import java.time.LocalDate
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory

class ReskWsClient : WebServiceGatewaySupport() {

    fun rohPrSakPrBarn(saksnummer: Int): CResknObjectHolder? {
        val request = RohPrSakPrBarn().apply {
            naksjonsKode = 1
            nBidragsSaksnr = saksnummer
        }
        val rohTransPrSakResponse = webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohPrSakPrBarn"),
        ) as RohTransPrSakResponse

        return rohTransPrSakResponse.rohTransPrSakResult
    }

    fun rohPrPersPrSakPrBarn(fødselsnummer: String): CResknObjectHolder? {
        val request = RohPrPersPrSakPrBarn().apply {
            naksjonsKode = 2
            sfnrorgnr = fødselsnummer
        }
        val rohPrPersPrSakPrBarnResponse = webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohPrPersPrSakPrBarn"),
        ) as RohPrPersPrSakPrBarnResponse
        return rohPrPersPrSakPrBarnResponse.rohPrPersPrSakPrBarnResult
    }

    fun rohTransPrSak(saksnummer: Int): CResknObjectHolder? {
        val request = RohTransPrSak().apply {
            naksjonsKode = 3
            nBidragsSaksnr = saksnummer
            dtdatoFom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(1900, 1, 1))
            dtdatoTom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(9999, 1, 1))
            nmaxReturTrans = Int.MAX_VALUE
        }
        val rohTransPrSakResponse = webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohTransPrSak"),
        ) as RohTransPrSakResponse
        return rohTransPrSakResponse.rohTransPrSakResult
    }

    fun rohTransPrPersPrOrg(fødselsnummer: String): CResknObjectHolder? {
        val request = RohTransPrPersPrOrg().apply {
            naksjonsKode = 4
            sfnrorgnr = fødselsnummer
            dtdatoFom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(1900, 1, 1))
            dtdatoTom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(9999, 1, 1))
            nmaxReturTrans = Int.MAX_VALUE
        }
        val rohTransPrPersPrOrgResponse = webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohTransPrPersPrOrg"),
        ) as RohTransPrPersPrOrgResponse
        return rohTransPrPersPrOrgResponse.rohTransPrPersPrOrgResult
    }

    fun rohTransPrTransID(transaksjonsid: String): CResknObjectHolder? {
        val request = RohTransPrTransID().apply {
            naksjonsKode = 5
            sTransID = transaksjonsid
            dtdatoFom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(1900, 1, 1))
            dtdatoTom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(9999, 1, 1))
        }
        val rohTransPrTransIDResponse = webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohTransPrTransID"),
        ) as RohTransPrTransIDResponse
        return rohTransPrTransIDResponse.rohTransPrTransIDResult
    }

    fun rohInnkrevInfo(fødselsnummer: String): CResknObjectHolder? {
        val request = RohInnkrevInfo().apply {
            naksjonsKode = 6
            sfnrorgnr = fødselsnummer
        }
        val rohInnkrevInfoResponse = webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohInnkrevInfo"),
        ) as RohInnkrevInfoResponse
        return rohInnkrevInfoResponse.rohInnkrevInfoResult
    }

    fun rohRMEndreFNR(saksnummer: Int, fødselsnummerBarn: String, fødselsnummerNyRm: String): CResknObjectHolder? {
        val now = LocalDate.now()
        val request = RohRMEndreFNR().apply {
            naksjonsKode = 8
            stypeEndring = "RM"
            nbidragSaksnr = saksnummer
            sfnr = "22222222226"
            sfnrGjelder = fødselsnummerBarn
            sfnrNy = fødselsnummerNyRm
            dtdatoGjelderFOM = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(now.year, now.dayOfMonth, now.dayOfMonth))
        }
        val rohRMEndreFNRResponse = webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohRMEndreFNR"),
        ) as RohRMEndreFNRResponse
        return rohRMEndreFNRResponse.rohRMEndreFNRResult
    }
}
