package no.nav.bidrag.elin.stub.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val LOGGER = KotlinLogging.logger { }

@RestControllerAdvice
class DefaultRestControllerAdvice {

    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(exception: JwtTokenUnauthorizedException): ResponseEntity<Any> {
        LOGGER.warn(exception) { "Ugyldig eller manglende sikkerhetstoken" }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WARNING, "Ugyldig eller manglende sikkerhetstoken").build()
    }

    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(exception: Exception): ResponseEntity<Any> {
        LOGGER.warn(exception) { "Det skjedde en ukjent feil" }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil")
            .build()
    }
}
