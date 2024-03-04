package no.nav.bidrag.reskontrolegacy.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.web.DefaultCorsFilter
import no.nav.bidrag.commons.web.MdcFilter
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.spn.www.BisysReskWSSoapProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@OpenAPIDefinition(info = Info(title = "bidrag-reskontro-legacy", version = "v1"), security = [SecurityRequirement(name = "bearer-key")])
@SecurityScheme(bearerFormat = "JWT", name = "bearer-key", scheme = "bearer", type = SecuritySchemeType.HTTP)
@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
@Import(DefaultCorsFilter::class, MdcFilter::class)
class ReskontroLegacyConfig {

    @Bean
    fun reskWsSoapProxy(@Value("\${ELIN_URL}") elinUrl: String): BisysReskWSSoapProxy {
        return BisysReskWSSoapProxy(elinUrl)
    }
}
