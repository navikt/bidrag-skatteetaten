package no.nav.bidrag.reskontro

import no.nav.bidrag.reskontro.config.RestConfig
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@EnableAutoConfiguration
@ComponentScan(
    basePackageClasses = [BidragReskontro::class],
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [RestConfig::class])],
)
class TestBidragReskontro
