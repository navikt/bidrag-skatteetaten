package no.nav.bidrag.reskontro.aop

import no.nav.bidrag.commons.security.maskinporten.MaskinportenClientException
import no.nav.bidrag.reskontro.exceptions.FeilMotSkattException
import no.nav.bidrag.reskontro.exceptions.IngenDataFraSkattException
import no.nav.bidrag.reskontro.exceptions.TimeoutFraSkattException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpStatusCodeException

private fun String?.sanitizeHeader(): String = this?.replace("\r", "")?.replace("\n", " ") ?: ""

@RestControllerAdvice
class DefaultRestControllerAdvice {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultRestControllerAdvice::class.java)
    }

    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpClientErrorException(exception: HttpStatusCodeException): ResponseEntity<Any> {
        val errorMessage = getErrorMessage(exception)
        LOGGER.warn(errorMessage, exception)
        return ResponseEntity
            .status(exception.statusCode)
            .build()
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

    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(exception: JwtTokenUnauthorizedException): ResponseEntity<Any> {
        LOGGER.warn("Ugyldig eller manglende sikkerhetstoken", exception)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WARNING, "Ugyldig eller manglende sikkerhetstoken")
            .build()
    }

    @ExceptionHandler(IngenDataFraSkattException::class)
    fun handleIngenDataFraSkattException(exception: IngenDataFraSkattException): ResponseEntity<Any> = ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .header(HttpHeaders.WARNING, "Fant ingen data: ${exception.message.sanitizeHeader()}")
        .build()

    @ExceptionHandler(MaskinportenClientException::class)
    fun handleMaskinportenClientException(exception: MaskinportenClientException): ResponseEntity<Any> = ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .header(HttpHeaders.WARNING, "Feil i maskinportentoken benyttet mot skatt: ${exception.message.sanitizeHeader()}")
        .build()

    @ExceptionHandler(TimeoutFraSkattException::class)
    fun handleTimeoutFraSkattException(exception: TimeoutFraSkattException): ResponseEntity<Any> = ResponseEntity
        .status(HttpStatus.BAD_GATEWAY)
        .header(HttpHeaders.WARNING, "Timeout mot skatt: ${exception.message.sanitizeHeader()}")
        .build()

    @ExceptionHandler(FeilMotSkattException::class)
    fun handleFeilMotSkattException(exception: FeilMotSkattException): ResponseEntity<Any> = ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .header(HttpHeaders.WARNING, "Feil ved kall mot skatt: ${exception.message.sanitizeHeader()}, cause: ${exception.cause}")
        .build()

    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(exception: Exception): ResponseEntity<Any> {
        LOGGER.warn("Det skjedde en ukjent feil: ${exception.message} ${exception.stackTraceToString()}", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil")
            .build()
    }
}
