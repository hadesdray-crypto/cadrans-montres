package fr.cadrans

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class CadransTest {
    private val vendredi13 = GregorianCalendar(2026, Calendar.NOVEMBER, 13, 9, 5, 7)

    @Test fun jourEnFrancais() = assertEquals("vendredi", DateFrancaise.jour(vendredi13))
    @Test fun moisEnFrancais() = assertEquals("novembre", DateFrancaise.mois(vendredi13))
    @Test fun semaineIso() = assertEquals(46, DateFrancaise.semaine(vendredi13))
    @Test fun semaineIsoDebutAnnee() = assertEquals(1, DateFrancaise.semaine(GregorianCalendar(2026, Calendar.JANUARY, 1)))
    @Test fun heure24() = assertEquals("09:05", DateFrancaise.heure(vendredi13, true, false))
    @Test fun heure24Secondes() = assertEquals("09:05:07", DateFrancaise.heure(vendredi13, true, true))
    @Test fun heure12MinuitEtMidi() {
        assertEquals("12:00", DateFrancaise.heure(GregorianCalendar(2026, 0, 1, 0, 0, 0), false, false))
        assertEquals("12:30", DateFrancaise.heure(GregorianCalendar(2026, 0, 1, 12, 30, 0), false, false))
        assertEquals("3:30", DateFrancaise.heure(GregorianCalendar(2026, 0, 1, 15, 30, 0), false, false))
    }
    @Test fun tousLesJoursEtMoisSontFrancais() {
        val c = GregorianCalendar(2026, Calendar.JANUARY, 1)
        val jours = (0 until 7).map { d -> c.clone() as Calendar }.mapIndexed { i, cal -> cal.add(Calendar.DAY_OF_MONTH, i); DateFrancaise.jour(cal) }
        assertEquals(7, jours.toSet().size)
        val mois = (0 until 12).map { m -> DateFrancaise.mois(GregorianCalendar(2026, m, 1)) }
        assertEquals(12, mois.toSet().size)
        assertTrue(mois.contains("août") && mois.contains("décembre") && mois.contains("février"))
    }

    private fun base() = Cadran(nom = "t", couleurFond = 1, couleurAiguilles = 2, couleurTexte = 3, couleurAccent = 4)

    @Test fun ligneDateComplete() =
        assertEquals("vendredi 13 novembre", DateFrancaise.ligneDate(base().copy(mois = true), vendredi13))

    @Test fun ligneDateMajuscules() =
        assertEquals("VENDREDI 13", DateFrancaise.ligneDate(base().copy(majuscules = true), vendredi13))

    @Test fun ligneDateVideSiToutEstCache() =
        assertEquals("", DateFrancaise.ligneDate(base().copy(jour = false, date = false), vendredi13))

    @Test fun lignesInfos() {
        val l = DateFrancaise.lignes(base().copy(semaine = true, batterie = true, pas = true), vendredi13, 78, 6432)
        assertEquals(4, l.size)
        assertEquals("semaine 46", l[1])
        assertEquals("batterie 78 %", l[2])
        assertTrue(l[3].endsWith(" pas"))
    }

    @Test fun jsonAllerRetour() {
        val c = base().copy(nom = "Mon cadran", forme = Forme.CARRE, affichage = Affichage.MIXTE, mois = true, pas = true, format24h = false)
        assertEquals(c, Cadran.depuisJson(c.versJson()))
    }

    @Test fun listeJsonAllerRetour() {
        val l = listOf(base().copy(nom = "a"), base().copy(nom = "b", batterie = true))
        assertEquals(l, Cadran.listeDepuisJson(Cadran.listeVersJson(l)))
    }

    @Test fun jsonAbimeNePlantePas() {
        assertTrue(Cadran.listeDepuisJson("pas du json").isEmpty())
        assertTrue(Cadran.listeDepuisJson(null).isEmpty())
        assertTrue(Cadran.listeDepuisJson("").isEmpty())
    }

    @Test fun modelesValides() {
        assertTrue(Modeles.liste.size >= 6)
        assertEquals(Modeles.liste.size, Modeles.liste.map { it.nom }.toSet().size)
        assertTrue(Modeles.palette.size >= 12)
    }

    @Test fun cataloguesNonVides() {
        assertTrue(Catalogue.marques.isNotEmpty() && Catalogue.boutiques.isNotEmpty())
    }
}