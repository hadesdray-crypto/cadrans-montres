package fr.cadrans

import android.content.Context

/** Mémorise les cadrans créés et les boutiques choisies, uniquement sur le téléphone. */
class Stockage(contexte: Context) {
    private val sp = contexte.getSharedPreferences("cadrans", Context.MODE_PRIVATE)

    fun charger(): List<Cadran> = Cadran.listeDepuisJson(sp.getString("liste", null))

    fun enregistrer(c: Cadran) {
        val l = charger().filter { it.nom != c.nom } + c
        sp.edit().putString("liste", Cadran.listeVersJson(l)).apply()
    }

    fun supprimer(nom: String) {
        sp.edit().putString("liste", Cadran.listeVersJson(charger().filter { it.nom != nom })).apply()
    }

    fun boutiqueChoisie(nom: String): Boolean = sp.getBoolean("boutique_$nom", false)

    fun choisirBoutique(nom: String, v: Boolean) {
        sp.edit().putBoolean("boutique_$nom", v).apply()
    }
}