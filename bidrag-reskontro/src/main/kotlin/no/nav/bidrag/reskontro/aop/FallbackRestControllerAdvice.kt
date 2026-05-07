package no.nav.bidrag.reskontro.aop

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class FallbackRestControllerAdvice {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FallbackRestControllerAdvice::class.java)
    }

    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(exception: Exception): ResponseEntity<Any> {
        LOGGER.warn("Det skjedde en ukjent feil: ${exception.message} ${exception.stackTraceToString()}", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil: ${exception.message}")
            .build()
    }
}

