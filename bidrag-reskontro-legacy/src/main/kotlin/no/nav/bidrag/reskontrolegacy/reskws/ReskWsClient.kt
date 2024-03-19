package no.nav.bidrag.reskontrolegacy.reskws

import no.nav.bidrag.reskontrolegacy.generated.CResknObjectHolder
import no.nav.bidrag.reskontrolegacy.generated.RohInnkrevInfo
import no.nav.bidrag.reskontrolegacy.generated.RohPrPersPrSakPrBarn
import no.nav.bidrag.reskontrolegacy.generated.RohPrSakPrBarn
import no.nav.bidrag.reskontrolegacy.generated.RohRMEndreFNR
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrPersPrOrg
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrSak
import no.nav.bidrag.reskontrolegacy.generated.RohTransPrTransID
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
        return webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohPrSakPrBarn"),
        ) as CResknObjectHolder?
    }

    fun rohPrPersPrSakPrBarn(fødselsnummer: String): CResknObjectHolder? {
        val request = RohPrPersPrSakPrBarn().apply {
            naksjonsKode = 2
            sfnrorgnr = fødselsnummer
        }
        return webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohPrPersPrSakPrBarn"),
        ) as CResknObjectHolder?
    }

    fun rohTransPrSak(saksnummer: Int): CResknObjectHolder? {
        val request = RohTransPrSak().apply {
            naksjonsKode = 3
            nBidragsSaksnr = saksnummer
            dtdatoFom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(1900, 1, 1))
            dtdatoTom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(9999, 1, 1))
            nmaxReturTrans = Int.MAX_VALUE
        }
        return webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohTransPrSak"),
        ) as CResknObjectHolder?
    }

    fun rohTransPrPersPrOrg(fødselsnummer: String): CResknObjectHolder? {
        val request = RohTransPrPersPrOrg().apply {
            naksjonsKode = 4
            sfnrorgnr = fødselsnummer
            dtdatoFom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(1900, 1, 1))
            dtdatoTom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(9999, 1, 1))
            nmaxReturTrans = Int.MAX_VALUE
        }
        return webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohTransPrPersPrOrg"),
        ) as CResknObjectHolder?
    }

    fun rohTransPrTransID(transaksjonsid: String): CResknObjectHolder? {
        val request = RohTransPrTransID().apply {
            naksjonsKode = 5
            sTransID = transaksjonsid
            dtdatoFom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(1900, 1, 1))
            dtdatoTom = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar(9999, 1, 1))
        }
        return webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohTransPrTransID"),
        ) as CResknObjectHolder?
    }

    fun rohInnkrevInfo(fødselsnummer: String): CResknObjectHolder? {
        val request = RohInnkrevInfo().apply {
            naksjonsKode = 6
            sfnrorgnr = fødselsnummer
        }
        return webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohInnkrevInfo"),
        ) as CResknObjectHolder?
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
        return webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("http://www.spn.no/rtv/webservices/BisysReskWS/rohRMEndreFNR"),
        ) as CResknObjectHolder?
    }
}
