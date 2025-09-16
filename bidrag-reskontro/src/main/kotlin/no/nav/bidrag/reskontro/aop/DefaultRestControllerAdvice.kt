package no.nav.bidrag.reskontro.aop

import no.nav.bidrag.commons.security.maskinporten.MaskinportenClientException
import no.nav.bidrag.reskontro.exceptions.IngenDataFraSkattException
import no.nav.bidrag.reskontro.exceptions.TimeoutFraSkattException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpStatusCodeException

@RestControllerAdvice
class DefaultRestControllerAdvice {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultRestControllerAdvice::class.java)
    }

    @ResponseBody
    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpClientErrorException(exception: HttpStatusCodeException): ResponseEntity<*> {
        val errorMessage = getErrorMessage(exception)
        LOGGER.warn(errorMessage, exception)
        return ResponseEntity
            .status(exception.statusCode)
            .header(HttpHeaders.WARNING, errorMessage)
            .build<Any>()
    }

    private fun getErrorMessage(exception: HttpStatusCodeException): String {
        val errorMessage = StringBuilder()
        errorMessage.append("Det skjedde en feil ved kall mot ekstern tjeneste: ")
        exception.responseHeaders?.get(HttpHeaders.WARNING)?.firstOrNull()?.let { errorMessage.append(it) }
        if (exception.statusText.isNotEmpty()) {
            errorMessage.append(" - ")
            errorMessage.append(exception.statusText)
        }
        return errorMessage.toString()
    }

    @ResponseBody
    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(exception: Exception): ResponseEntity<*> {
        LOGGER.warn("Det skjedde en ukjent feil: ${exception.message} ${exception.stackTraceToString()}", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil: ${exception.message}")
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(exception: JwtTokenUnauthorizedException): ResponseEntity<*> {
        LOGGER.warn("Ugyldig eller manglende sikkerhetstoken", exception)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WARNING, "Ugyldig eller manglende sikkerhetstoken")
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(IngenDataFraSkattException::class)
    fun handleIngenDataFraSkattException(exception: IngenDataFraSkattException): ResponseEntity<*> = ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .header(HttpHeaders.WARNING, "Fant ingen data: ${exception.message}")
        .build<Any>()

    @ResponseBody
    @ExceptionHandler(MaskinportenClientException::class)
    fun handleMaskinportenClientException(exception: MaskinportenClientException): ResponseEntity<*> = ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .header(HttpHeaders.WARNING, "Feil i maskinportentoken benyttet mot skatt: ${exception.message}")
        .build<Any>()

    @ResponseBody
    @ExceptionHandler(TimeoutFraSkattException::class)
    fun handleMaskinportenClientException(exception: TimeoutFraSkattException): ResponseEntity<*> = ResponseEntity
        .status(HttpStatus.BAD_GATEWAY)
        .header(HttpHeaders.WARNING, "Timeout mot skatt: ${exception.message}")
        .build<Any>()
}
