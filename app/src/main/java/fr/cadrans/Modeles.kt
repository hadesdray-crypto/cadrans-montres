package fr.cadrans

import android.graphics.Color

/** Modèles prêts à l'emploi : l'utilisateur en choisit un puis règle ce qu'il veut. */
object Modeles {
    val liste: List<Cadran> = listOf(
        Cadran(
            nom = "Classique", couleurFond = Color.parseColor("#101820"), couleurAiguilles = Color.WHITE,
            couleurTexte = Color.WHITE, couleurAccent = Color.parseColor("#F5C542")
        ),
        Cadran(
            nom = "Sport", affichage = Affichage.MIXTE, couleurFond = Color.BLACK, couleurAiguilles = Color.parseColor("#FF5A36"),
            couleurTexte = Color.WHITE, couleurAccent = Color.parseColor("#FF5A36"), semaine = false, batterie = true, pas = true, mois = false
        ),
        Cadran(
            nom = "Chiffres géants", affichage = Affichage.NUMERIQUE, couleurFond = Color.parseColor("#0B1D3A"),
            couleurAiguilles = Color.WHITE, couleurTexte = Color.WHITE, couleurAccent = Color.parseColor("#4FC3F7"),
            mois = true, secondes = false
        ),
        Cadran(
            nom = "Minimal", couleurFond = Color.WHITE, couleurAiguilles = Color.BLACK, couleurTexte = Color.parseColor("#444444"),
            couleurAccent = Color.parseColor("#999999"), jour = false, date = false, secondes = false, chiffres = false
        ),
        Cadran(
            nom = "Rétro", forme = Forme.CARRE, couleurFond = Color.parseColor("#F3E9D2"), couleurAiguilles = Color.parseColor("#3B2A1A"),
            couleurTexte = Color.parseColor("#3B2A1A"), couleurAccent = Color.parseColor("#A0522D"), mois = true
        ),
        Cadran(
            nom = "Enfant", affichage = Affichage.MIXTE, couleurFond = Color.parseColor("#7E57C2"), couleurAiguilles = Color.parseColor("#FFEB3B"),
            couleurTexte = Color.WHITE, couleurAccent = Color.parseColor("#FF4081"), mois = true
        ),
        Cadran(
            nom = "Nuit", affichage = Affichage.NUMERIQUE, couleurFond = Color.BLACK, couleurAiguilles = Color.parseColor("#2E7D32"),
            couleurTexte = Color.parseColor("#66BB6A"), couleurAccent = Color.parseColor("#2E7D32"), secondes = false, batterie = true
        )
    )

    /** Couleurs proposées dans l'éditeur (assez grosses pour être touchées facilement). */
    val palette: List<Int> = listOf(
        "#FFFFFF", "#000000", "#101820", "#0B1D3A", "#E53935", "#FF5A36", "#F5C542", "#FFEB3B",
        "#66BB6A", "#2E7D32", "#4FC3F7", "#1E88E5", "#7E57C2", "#FF4081", "#F3E9D2", "#999999"
    ).map { Color.parseColor(it) }
}