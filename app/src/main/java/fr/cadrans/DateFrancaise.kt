package fr.cadrans

import java.util.Calendar
import java.util.Locale

/** Jours et mois écrits en français, sans dépendre de la langue du téléphone. */
object DateFrancaise {
    private val jours = arrayOf("dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi")
    private val moisNoms = arrayOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )

    fun jour(c: Calendar): String = jours[c.get(Calendar.DAY_OF_WEEK) - 1]

    fun mois(c: Calendar): String = moisNoms[c.get(Calendar.MONTH)]

    /** Numéro de semaine à la française (norme ISO : lundi premier jour, semaine 1 = celle du premier jeudi). */
    fun semaine(c: Calendar): Int {
        val copie = c.clone() as Calendar
        copie.firstDayOfWeek = Calendar.MONDAY
        copie.minimalDaysInFirstWeek = 4
        return copie.get(Calendar.WEEK_OF_YEAR)
    }

    fun heure(c: Calendar, format24h: Boolean, avecSecondes: Boolean): String {
        val h = c.get(Calendar.HOUR_OF_DAY)
        val m = c.get(Calendar.MINUTE)
        val s = c.get(Calendar.SECOND)
        val hh = if (format24h) String.format(Locale.FRANCE, "%02d", h) else (if (h % 12 == 0) 12 else h % 12).toString()
        val base = hh + ":" + String.format(Locale.FRANCE, "%02d", m)
        return if (avecSecondes) base + ":" + String.format(Locale.FRANCE, "%02d", s) else base
    }

    fun ligneDate(cadran: Cadran, c: Calendar): String {
        val parts = ArrayList<String>()
        if (cadran.jour) parts.add(jour(c))
        if (cadran.date) parts.add(c.get(Calendar.DAY_OF_MONTH).toString())
        if (cadran.mois) parts.add(mois(c))
        val s = parts.joinToString(" ")
        return if (cadran.majuscules) s.uppercase(Locale.FRANCE) else s
    }

    fun lignes(cadran: Cadran, c: Calendar, batterie: Int, pas: Int): List<String> {
        val l = ArrayList<String>()
        val d = ligneDate(cadran, c)
        if (d.isNotEmpty()) l.add(d)
        if (cadran.semaine) l.add(if (cadran.majuscules) "SEMAINE " + semaine(c) else "semaine " + semaine(c))
        if (cadran.batterie) l.add(if (cadran.majuscules) "BATTERIE $batterie %" else "batterie $batterie %")
        if (cadran.pas) l.add(String.format(Locale.FRANCE, "%,d", pas) + if (cadran.majuscules) " PAS" else " pas")
        return l
    }
}