package no.nav.bidrag.aktoerregister.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity(name = "dodsbo")
data class Dødsbo(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "kontaktperson")
    val kontaktperson: String? = null,

    @Column(name = "adresselinje1")
    val adresselinje1: String? = null,

    @Column(name = "adresselinje2")
    val adresselinje2: String? = null,

    @Column(name = "adresselinje3")
    val adresselinje3: String? = null,

    @Column(name = "leilighetsnummer")
    val leilighetsnummer: String? = null,

    @Column(name = "postnr")
    val postnr: String? = null,

    @Column(name = "poststed")
    val poststed: String? = null,

    @Column(name = "land")
    val land: String? = null,

    @OneToOne
    @JoinColumn(name = "aktoer_id")
    var aktør: Aktør? = null,

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dødsbo

        if (kontaktperson != other.kontaktperson) return false
        if (adresselinje1 != other.adresselinje1) return false
        if (adresselinje2 != other.adresselinje2) return false
        if (adresselinje3 != other.adresselinje3) return false
        if (leilighetsnummer != other.leilighetsnummer) return false
        if (postnr != other.postnr) return false
        if (poststed != other.poststed) return false
        if (land != other.land) return false
        if (aktør != other.aktør) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kontaktperson?.hashCode() ?: 0
        result = 31 * result + (adresselinje1?.hashCode() ?: 0)
        result = 31 * result + (adresselinje2?.hashCode() ?: 0)
        result = 31 * result + (adresselinje3?.hashCode() ?: 0)
        result = 31 * result + (leilighetsnummer?.hashCode() ?: 0)
        result = 31 * result + (postnr?.hashCode() ?: 0)
        result = 31 * result + (poststed?.hashCode() ?: 0)
        result = 31 * result + (land?.hashCode() ?: 0)
        result = 31 * result + (aktør?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Dødsbo(" +
            "id=$id, " +
            "kontaktperson=$kontaktperson, " +
            "adresselinje1=$adresselinje1, " +
            "adresselinje2=$adresselinje2, " +
            "adresselinje3=$adresselinje3, " +
            "leilighetsnummer=$leilighetsnummer, " +
            "postnr=$postnr, " +
            "poststed=$poststed, " +
            "land=$land, " +
            "aktør=${aktør?.id})"
    }
}
