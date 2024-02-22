package no.nav.bidrag.aktoerregister.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.aktoerregister.dto.enumer.Gradering

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AktoerDTO(

    @Schema(description = "Id for aktøren")
    val aktoerId: AktoerIdDTO,

    @Schema(description = "Offentlig id for samhandlere. Angis ikke for personer.")
    val offentligId: String? = null,

    @Schema(description = "Type offentlig id. F.eks ORG for norske organisasjonsnummere.")
    val offentligIdType: String? = null,

    @Schema(description = "Navn for aktøren")
    val navn: NavnDTO? = null,

    @Schema(description = "Gradering for aktøren")
    val gradering: Gradering? = null,

    @Schema(description = "Aktørens adresse. Angis ikke for personer.")
    val adresse: AdresseDTO? = null,

    @Schema(description = "Språkkoden for aktøren.")
    val sprakkode: String? = null,

    @Schema(description = "Lister alle aktørens tidligere identer.")
    val tidligereIdenter: List<AktoerIdDTO>? = null,

    @Schema(description = "Personens fødselsdato. Settes for alle personer der fødselsdato er kjent.")
    val fodtDato: String? = null,

    @Schema(description = "Personens fødselsdato. Settes for alle personer der fødselsdato er kjent.")
    val dodDato: String? = null,

    @Schema(description = "Dødsbo for aktøren")
    val dodsbo: DodsboDTO? = null,

    @Schema(description = "Aktørens kontonummer.")
    val kontonummer: KontonummerDTO? = null,
)
