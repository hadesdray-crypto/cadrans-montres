package fr.cadrans

/** Marques de montres et boutiques : ce qui est prêt, ce qui ne l'est pas. Statuts honnêtes, mis à jour à chaque bloc ajouté. */
data class Marque(val nom: String, val etat: String, val detail: String)
data class Boutique(val nom: String, val pour: String, val remarque: String)

object Catalogue {
    val marques = listOf(
        Marque("Wear OS (Samsung Galaxy Watch, Pixel Watch…)", "En préparation", "Aperçu et modèles prêts. Export vers la montre : prochain bloc."),
        Marque("Amazfit (Zepp OS)", "À étudier", "Cadrans en JavaScript, outils gratuits. Pas encore intégré."),
        Marque("Garmin", "À étudier", "Fabrication probablement sur ordinateur. Pas encore intégré."),
        Marque("Huawei", "À vérifier", "Pas encore étudié."),
        Marque("Xiaomi", "À vérifier", "Pas encore étudié."),
        Marque("Fitbit", "À vérifier", "Pas encore étudié."),
        Marque("Apple Watch", "À vérifier", "Les cadrans de tiers sont probablement impossibles. Pas confirmé.")
    )

    val boutiques = listOf(
        Boutique("Google Play", "Wear OS", "Compte développeur payant (25 $, une fois) : demandé seulement avec ton accord écrit."),
        Boutique("Garmin Connect IQ", "Garmin", "Envoi par une page web puis validation par Garmin. À confirmer."),
        Boutique("Zepp Store", "Amazfit", "Pas encore vérifié."),
        Boutique("Huawei AppGallery", "Huawei", "Pas encore vérifié."),
        Boutique("Xiaomi", "Xiaomi", "Pas encore vérifié.")
    )
}