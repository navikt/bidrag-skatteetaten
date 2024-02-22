package no.nav.bidrag.aktoerregister.util

import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

object ConsumerUtils {

    fun leggTilPathPåUri(url: URI, path: String?) = UriComponentsBuilder.fromUri(url)
        .path(path ?: "").build().toUri()
}
