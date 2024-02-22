package no.nav.bidrag.aktoerregister.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Version
import java.sql.Timestamp

@Entity
data class Hendelse(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sekvensnummer")
    val sekvensnummer: Int = 0,

    @ManyToOne
    @JoinColumn(name = "aktoer_id", referencedColumnName = "id")
    val aktør: Aktør,

    @Column(name = "aktoer_ident")
    val aktørIdent: String,

    @Version
    @Column(name = "sist_endret")
    val sistEndret: Timestamp? = null,

    @Column(name = "kontonummer_oppdatering")
    var kontonummerOppdatering: Boolean? = null,

    @Column(name = "ident_oppdatering")
    val identOppdatering: Boolean? = null,

    @Column(name = "navn_oppdatering")
    val navnOppdatering: Boolean? = null,

    @Column(name = "adresse_oppdatering")
    val adresseOppdatering: Boolean? = null,

    @Column(name = "fodt_dato_oppdatering")
    val fødtDatoOppdatering: Boolean? = null,

    @Column(name = "dod_dato_oppdatering")
    val dødDatoOppdatering: Boolean? = null,

    @Column(name = "gradering_oppdatering")
    val graderingOppdatering: Boolean? = null,

    @Column(name = "dodsbo_oppdatering")
    val dødsboOppdatering: Boolean? = null,

    @Column(name = "sprak_oppdatering")
    val språkOppdatering: Boolean? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Hendelse

        if (sekvensnummer != other.sekvensnummer) return false
        if (aktørIdent != other.aktørIdent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sekvensnummer
        result = 31 * result + aktørIdent.hashCode()
        return result
    }

    override fun toString(): String {
        return "Hendelse(" +
            "sekvensnummer=$sekvensnummer," +
            "aktør=${aktør.id}, " +
            "aktoerIdent='$aktørIdent', " +
            "sistEndret=$sistEndret)"
    }

    constructor(sekvensnummer: Int, aktør: Aktør) : this(sekvensnummer = sekvensnummer, aktør = aktør, aktørIdent = aktør.aktørIdent)
}
