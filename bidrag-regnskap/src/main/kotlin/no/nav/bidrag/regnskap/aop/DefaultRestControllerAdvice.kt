package no.nav.bidrag.regnskap.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.commons.security.maskinporten.MaskinportenClientException
import no.nav.bidrag.regnskap.util.PåløpException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpStatusCodeException

private val LOGGER = KotlinLogging.logger {}

@RestControllerAdvice
class DefaultRestControllerAdvice {

    companion object {
        private const val UNAUTHORIZED_MESSAGE = "Ugyldig eller manglende sikkerhetstoken"
        private const val UNKNOWN_ERROR_MESSAGE = "Det skjedde en ukjent feil"
        private const val MASKINPORTEN_ERROR_MESSAGE = "Noe gikk galt ved kall til maskinporten"
        private const val PÅLØP_ERROR_MESSAGE = "Ble stoppet grunnet påløp"
        private const val EXTERNAL_SERVICE_ERROR_PREFIX = "Det skjedde en feil ved kall mot ekstern tjeneste: "
    }

    @ResponseBody
    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpClientErrorException(exception: HttpStatusCodeException): ResponseEntity<*> {
        val errorMessage = getErrorMessage(exception)
        LOGGER.warn(exception) { errorMessage }
        return ResponseEntity.status(exception.statusCode).header(HttpHeaders.WARNING, errorMessage).build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(exception: JwtTokenUnauthorizedException): ResponseEntity<*> {
        LOGGER.warn(exception) { UNAUTHORIZED_MESSAGE }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.WARNING, UNAUTHORIZED_MESSAGE)
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<*> {
        LOGGER.warn(exception) { UNKNOWN_ERROR_MESSAGE }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "$UNKNOWN_ERROR_MESSAGE: ${exception.message}").build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(MaskinportenClientException::class)
    fun handleMaskinportenClientException(exception: MaskinportenClientException): ResponseEntity<*> {
        LOGGER.error(exception) { "$MASKINPORTEN_ERROR_MESSAGE: ${exception.message}" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "$MASKINPORTEN_ERROR_MESSAGE: ${exception.message}")
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(PåløpException::class)
    fun handlePåløpException(exception: PåløpException): ResponseEntity<*> {
        LOGGER.error(exception) { "$PÅLØP_ERROR_MESSAGE: ${exception.message}" }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .header(HttpHeaders.WARNING, "$PÅLØP_ERROR_MESSAGE: ${exception.message}")
            .build<Any>()
    }

    private fun getErrorMessage(exception: HttpStatusCodeException): String {
        val errorMessage = StringBuilder()
        errorMessage.append(EXTERNAL_SERVICE_ERROR_PREFIX)
        exception.responseHeaders?.get(HttpHeaders.WARNING)?.firstOrNull()?.let { errorMessage.append(it) }
        if (exception.statusText.isNotEmpty()) {
            errorMessage.append(exception.statusText)
        }
        if (exception.message?.isNotEmpty() == true) {
            errorMessage.append(exception.message)
        }
        return errorMessage.toString()
    }
}
