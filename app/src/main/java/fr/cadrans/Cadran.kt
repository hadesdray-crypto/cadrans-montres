package fr.cadrans

import org.json.JSONArray
import org.json.JSONObject

enum class Forme(val libelle: String) { ROND("Ronde"), CARRE("Carrée") }

enum class Affichage(val libelle: String) {
    ANALOGIQUE("Aiguilles"),
    NUMERIQUE("Chiffres"),
    MIXTE("Les deux")
}

/** Un cadran de montre : tout ce qui se voit et se règle. Les couleurs sont des entiers ARGB. */
data class Cadran(
    val nom: String,
    val forme: Forme = Forme.ROND,
    val affichage: Affichage = Affichage.ANALOGIQUE,
    val couleurFond: Int,
    val couleurAiguilles: Int,
    val couleurTexte: Int,
    val couleurAccent: Int,
    val jour: Boolean = true,
    val date: Boolean = true,
    val mois: Boolean = false,
    val semaine: Boolean = false,
    val batterie: Boolean = false,
    val pas: Boolean = false,
    val secondes: Boolean = true,
    val majuscules: Boolean = false,
    val graduations: Boolean = true,
    val chiffres: Boolean = true,
    val format24h: Boolean = true
) {
    fun versJson(): JSONObject = JSONObject().apply {
        put("nom", nom)
        put("forme", forme.name)
        put("affichage", affichage.name)
        put("couleurFond", couleurFond)
        put("couleurAiguilles", couleurAiguilles)
        put("couleurTexte", couleurTexte)
        put("couleurAccent", couleurAccent)
        put("jour", jour)
        put("date", date)
        put("mois", mois)
        put("semaine", semaine)
        put("batterie", batterie)
        put("pas", pas)
        put("secondes", secondes)
        put("majuscules", majuscules)
        put("graduations", graduations)
        put("chiffres", chiffres)
        put("format24h", format24h)
    }

    companion object {
        /** Lecture tolérante : une valeur absente ou abîmée prend la valeur du modèle de base. */
        fun depuisJson(o: JSONObject, base: Cadran = Modeles.liste[0]): Cadran = Cadran(
            nom = o.optString("nom", base.nom).ifBlank { base.nom },
            forme = enumOu(o.optString("forme"), base.forme),
            affichage = enumOu(o.optString("affichage"), base.affichage),
            couleurFond = o.optInt("couleurFond", base.couleurFond),
            couleurAiguilles = o.optInt("couleurAiguilles", base.couleurAiguilles),
            couleurTexte = o.optInt("couleurTexte", base.couleurTexte),
            couleurAccent = o.optInt("couleurAccent", base.couleurAccent),
            jour = o.optBoolean("jour", base.jour),
            date = o.optBoolean("date", base.date),
            mois = o.optBoolean("mois", base.mois),
            semaine = o.optBoolean("semaine", base.semaine),
            batterie = o.optBoolean("batterie", base.batterie),
            pas = o.optBoolean("pas", base.pas),
            secondes = o.optBoolean("secondes", base.secondes),
            majuscules = o.optBoolean("majuscules", base.majuscules),
            graduations = o.optBoolean("graduations", base.graduations),
            chiffres = o.optBoolean("chiffres", base.chiffres),
            format24h = o.optBoolean("format24h", base.format24h)
        )

        fun listeVersJson(l: List<Cadran>): String = JSONArray().apply { l.forEach { put(it.versJson()) } }.toString()

        fun listeDepuisJson(s: String?): List<Cadran> {
            if (s.isNullOrBlank()) return emptyList()
            return try {
                val a = JSONArray(s)
                (0 until a.length()).mapNotNull { i -> a.optJSONObject(i)?.let { depuisJson(it) } }
            } catch (e: Exception) {
                emptyList()
            }
        }

        private inline fun <reified E : Enum<E>> enumOu(s: String, defaut: E): E =
            enumValues<E>().firstOrNull { it.name == s } ?: defaut
    }
}