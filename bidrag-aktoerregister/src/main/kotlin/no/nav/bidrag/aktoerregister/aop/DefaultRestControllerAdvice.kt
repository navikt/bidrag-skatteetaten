package no.nav.bidrag.aktoerregister.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.server.ResponseStatusException

private val LOGGER = KotlinLogging.logger {}

private fun String?.sanitizeHeader(): String = this?.replace("\r", "")?.replace("\n", " ") ?: ""

@RestControllerAdvice
class DefaultRestControllerAdvice {

    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpClientErrorException(exception: HttpStatusCodeException): ResponseEntity<Any> {
        val errorMessage = getErrorMessage(exception)
        LOGGER.warn(exception) { errorMessage }
        return ResponseEntity.status(exception.statusCode).header(HttpHeaders.WARNING, errorMessage.sanitizeHeader()).build()
    }

    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(exception: JwtTokenUnauthorizedException?): ResponseEntity<Any> {
        LOGGER.warn(exception) { "Ugyldig eller manglende sikkerhetstoken" }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.WARNING, "Ugyldig eller manglende sikkerhetstoken").build()
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handlerResponseStatusException(exception: ResponseStatusException): ResponseEntity<Any> {
        LOGGER.warn(exception) { exception.message }
        return ResponseEntity.status(exception.statusCode).header(HttpHeaders.WARNING, exception.message.sanitizeHeader()).build()
    }

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<Any> {
        LOGGER.warn(exception) { "Det skjedde en ukjent feil" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil: ${exception.message.sanitizeHeader()} Stacktrace: ${exception.stackTraceToString().sanitizeHeader()}")
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
}
