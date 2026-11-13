package fr.cadrans

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import java.util.Calendar

class MainActivity : Activity() {
    private val pile = ArrayList<Pair<String, () -> Unit>>()
    private lateinit var stock: Stockage
    private var courant: Cadran = Modeles.liste[0]
    private var plusOptions = false
    private var minutesEssai: Int? = null
    private var montre: MontreVirtuelleView? = null
    private var controles: LinearLayout? = null
    private var defil: ScrollView? = null

    private val bleu = Color.parseColor("#1E88E5")
    private val gris = Color.parseColor("#E0E0E0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stock = Stockage(this)
        aller("Accueil") { montrerAccueil() }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onBackPressed() = retour()

    // ---------- Navigation : un écran à la fois, retour toujours possible ----------

    private fun aller(nom: String, ecran: () -> Unit) {
        pile.add(Pair(nom, ecran))
        ecran()
    }

    private fun retour() {
        if (pile.size > 1) {
            pile.removeAt(pile.size - 1)
            pile.last().second()
        } else {
            AlertDialog.Builder(this)
                .setMessage("Quitter l'application ?")
                .setPositiveButton("Quitter") { _, _ -> finish() }
                .setNegativeButton("Rester", null)
                .show()
        }
    }

    // ---------- Éléments d'interface ----------

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun colonne(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

    private fun cadre(contenu: View) {
        val racine = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            @Suppress("DEPRECATION")
            setOnApplyWindowInsetsListener { v, ins ->
                v.setPadding(0, ins.systemWindowInsetTop, 0, ins.systemWindowInsetBottom)
                ins
            }
        }
        racine.addView(contenu, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(racine)
        racine.requestApplyInsets()
    }

    private fun texte(s: String, taille: Float = 17f, couleur: Int = Color.parseColor("#222222"), gras: Boolean = false): TextView =
        TextView(this).apply {
            text = s
            textSize = taille
            setTextColor(couleur)
            if (gras) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dp(16), dp(8), dp(16), dp(4))
        }

    private fun fond(couleur: Int): GradientDrawable =
        GradientDrawable().apply { setColor(couleur); cornerRadius = dp(14).toFloat() }

    private fun bouton(s: String, choisi: Boolean = false, action: () -> Unit): Button = Button(this).apply {
        text = s
        textSize = 17f
        isAllCaps = false
        minHeight = dp(52)
        setTextColor(if (choisi) Color.WHITE else Color.BLACK)
        background = fond(if (choisi) bleu else gris)
        setOnClickListener { action() }
    }

    private fun margeBouton(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(dp(16), dp(6), dp(16), dp(6))
        }

    private fun barre(titre: String, nomEcran: String): View {
        val l = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        l.addView(bouton("← Retour") { retour() }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(8), dp(6), dp(8), dp(6)) })
        l.addView(texte(titre, 20f, gras = true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        l.addView(bouton("?") { aide(nomEcran) }, LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(8), dp(6), dp(8), dp(6)) })
        return l
    }

    private fun aide(nomEcran: String) {
        AlertDialog.Builder(this)
            .setTitle("Un problème ?")
            .setMessage("Écris-nous en disant que tu es sur l'écran « $nomEcran » et ce qui ne va pas.\n\nAdresse : [à compléter]")
            .setPositiveButton("Fermer", null)
            .show()
    }

    private fun interrupteur(s: String, valeur: Boolean, action: (Boolean) -> Unit): Switch = Switch(this).apply {
        text = s
        textSize = 17f
        isChecked = valeur
        minHeight = dp(52)
        setPadding(dp(16), dp(4), dp(16), dp(4))
        setOnCheckedChangeListener { _, v -> action(v) }
    }

    // ---------- Accueil ----------

    private fun montrerAccueil() {
        val col = colonne()
        col.addView(texte("Cadrans", 30f, gras = true))
        col.addView(texte("Crée des cadrans de montre, en français, et vois-les tout de suite sur une montre virtuelle.", 17f))
        col.addView(bouton("Créer un cadran") { courant = Modeles.liste[0]; minutesEssai = null; aller("Éditeur") { montrerEditeur() } }, margeBouton())
        col.addView(bouton("Mes cadrans") { aller("Mes cadrans") { montrerMesCadrans() } }, margeBouton())
        col.addView(bouton("Marques de montres") { aller("Marques") { montrerMarques() } }, margeBouton())
        col.addView(bouton("Boutiques (diffusion)") { aller("Boutiques") { montrerBoutiques() } }, margeBouton())
        col.addView(bouton("?") { aide("Accueil") }, margeBouton())
        val d = ScrollView(this)
        d.addView(col)
        cadre(d)
    }

    // ---------- Éditeur ----------

    private fun maj(nouveau: Cadran) {
        courant = nouveau
        montre?.cadran = nouveau
    }

    private fun montrerEditeur() {
        val racine = colonne()
        racine.addView(barre("Créer un cadran", "Éditeur"))
        val m = MontreVirtuelleView(this)
        m.cadran = courant
        m.minutesEssai = minutesEssai
        montre = m
        val taille = minOf(resources.displayMetrics.widthPixels * 0.6f, resources.displayMetrics.heightPixels * 0.30f).toInt()
        racine.addView(m, LinearLayout.LayoutParams(taille, taille).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(4) })

        // Essai de l'heure : titre et bouton sur une ligne, curseur pleine largeur dessous
        val essai = colonne()
        val ligneEssai = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        ligneEssai.addView(texte("Heure d'essai : glisse pour changer l'heure", 15f), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        ligneEssai.addView(bouton("Heure réelle") { minutesEssai = null; m.minutesEssai = null }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(4), 0, dp(8), 0) })
        essai.addView(ligneEssai)
        val barreHeure = SeekBar(this).apply {
            max = 1439
            val c = Calendar.getInstance()
            progress = minutesEssai ?: (c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE))
            setPadding(dp(24), dp(8), dp(24), dp(8))
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, v: Int, fromUser: Boolean) {
                    if (fromUser) { minutesEssai = v; m.minutesEssai = v }
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        essai.addView(barreHeure, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        racine.addView(essai)

        val d = ScrollView(this)
        val c = colonne()
        d.addView(c)
        defil = d
        controles = c
        racine.addView(d, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        cadre(racine)
        construireControles()
    }

    private fun rafraichir() {
        val d = defil ?: return
        val c = controles ?: return
        val y = d.scrollY
        c.removeAllViews()
        construireControles()
        d.post { d.scrollTo(0, y) }
    }

    private fun construireControles() {
        val c = controles ?: return

        // Nom
        c.addView(texte("Nom de ton cadran", 15f, Color.GRAY))
        val nom = EditText(this).apply {
            setText(courant.nom)
            textSize = 18f
            setSingleLine(true)
            setPadding(dp(16), dp(10), dp(16), dp(10))
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, n: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, n: Int) {}
                override fun afterTextChanged(s: Editable?) { courant = courant.copy(nom = s?.toString() ?: "") }
            })
        }
        c.addView(nom, margeBouton())

        // Modèles
        c.addView(texte("1. Choisis un modèle", 18f, gras = true))
        val h = HorizontalScrollView(this)
        val ligne = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Modeles.liste.forEach { mod ->
            ligne.addView(bouton(mod.nom, courant.nom == mod.nom) {
                maj(mod.copy(forme = courant.forme)); rafraichir()
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(8), dp(4), dp(4), dp(4)) })
        }
        h.addView(ligne)
        c.addView(h)

        // Forme
        c.addView(texte("2. Forme de la montre", 18f, gras = true))
        val formes = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Forme.values().forEach { f ->
            formes.addView(bouton(f.libelle, courant.forme == f) { maj(courant.copy(forme = f)); rafraichir() },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(16), dp(4), dp(8), dp(4)) })
        }
        c.addView(formes)

        // Affichage
        c.addView(texte("3. Comment lire l'heure", 18f, gras = true))
        val aff = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Affichage.values().forEach { a ->
            aff.addView(bouton(a.libelle, courant.affichage == a) { maj(courant.copy(affichage = a)); rafraichir() },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(8), dp(4), dp(8), dp(4)) })
        }
        c.addView(aff)

        // Informations
        c.addView(texte("4. Ce qui s'affiche", 18f, gras = true))
        c.addView(interrupteur("Jour de la semaine (lundi…)", courant.jour) { maj(courant.copy(jour = it)) })
        c.addView(interrupteur("Numéro du jour (13)", courant.date) { maj(courant.copy(date = it)) })
        c.addView(interrupteur("Mois (novembre)", courant.mois) { maj(courant.copy(mois = it)) })
        c.addView(interrupteur("Numéro de semaine", courant.semaine) { maj(courant.copy(semaine = it)) })
        c.addView(interrupteur("Batterie", courant.batterie) { maj(courant.copy(batterie = it)) })
        c.addView(interrupteur("Nombre de pas", courant.pas) { maj(courant.copy(pas = it)) })

        // Couleurs
        c.addView(texte("5. Couleurs", 18f, gras = true))
        c.addView(ligneCouleurs("Fond", courant.couleurFond) { maj(courant.copy(couleurFond = it)); rafraichir() })
        c.addView(ligneCouleurs("Aiguilles", courant.couleurAiguilles) { maj(courant.copy(couleurAiguilles = it)); rafraichir() })
        c.addView(ligneCouleurs("Textes", courant.couleurTexte) { maj(courant.copy(couleurTexte = it)); rafraichir() })
        c.addView(ligneCouleurs("Détails (graduations, chiffres de l'heure)", courant.couleurAccent) { maj(courant.copy(couleurAccent = it)); rafraichir() })

        // Plus d'options
        c.addView(bouton(if (plusOptions) "Moins d'options" else "Plus d'options", false) { plusOptions = !plusOptions; rafraichir() }, margeBouton())
        if (plusOptions) {
            c.addView(interrupteur("Aiguille / secondes", courant.secondes) { maj(courant.copy(secondes = it)) })
            c.addView(interrupteur("Tout en MAJUSCULES", courant.majuscules) { maj(courant.copy(majuscules = it)) })
            c.addView(interrupteur("Petits traits autour du cadran", courant.graduations) { maj(courant.copy(graduations = it)) })
            c.addView(interrupteur("Chiffres de 1 à 12", courant.chiffres) { maj(courant.copy(chiffres = it)) })
            c.addView(interrupteur("Heure sur 24 h (sinon 12 h)", courant.format24h) { maj(courant.copy(format24h = it)) })
        }

        // Enregistrer
        c.addView(bouton("Enregistrer mon cadran", true) {
            val n = courant.nom.trim().ifBlank { "Mon cadran" }
            courant = courant.copy(nom = n)
            stock.enregistrer(courant)
            AlertDialog.Builder(this).setMessage("Cadran « $n » enregistré.").setPositiveButton("OK", null).show()
        }, margeBouton())
        c.addView(texte("Envoi vers la montre : bientôt (prochain bloc).", 14f, Color.GRAY))
    }

    private fun ligneCouleurs(titre: String, actuelle: Int, choix: (Int) -> Unit): View {
        val col = colonne()
        col.addView(texte(titre, 15f, Color.GRAY))
        val h = HorizontalScrollView(this)
        val l = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Modeles.palette.forEachIndexed { i, couleur ->
            val v = View(this)
            v.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(couleur)
                setStroke(dp(if (couleur == actuelle) 4 else 1), if (couleur == actuelle) bleu else Color.GRAY)
            }
            v.contentDescription = "$titre : couleur ${i + 1}"
            v.setOnClickListener { choix(couleur) }
            l.addView(v, LinearLayout.LayoutParams(dp(48), dp(48)).apply { setMargins(dp(6), dp(4), dp(6), dp(4)) })
        }
        h.addView(l)
        col.addView(h)
        return col
    }

    // ---------- Mes cadrans ----------

    private fun montrerMesCadrans() {
        val col = colonne()
        col.addView(barre("Mes cadrans", "Mes cadrans"))
        val liste = stock.charger()
        if (liste.isEmpty()) {
            col.addView(texte("Tu n'as pas encore enregistré de cadran. Va dans « Créer un cadran ».", 17f))
        }
        liste.forEach { cd ->
            col.addView(texte(cd.nom, 19f, gras = true))
            val l = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            l.addView(bouton("Ouvrir") { courant = cd; minutesEssai = null; aller("Éditeur") { montrerEditeur() } },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(16), dp(2), dp(8), dp(6)) })
            l.addView(bouton("Supprimer") {
                AlertDialog.Builder(this)
                    .setMessage("Supprimer le cadran « ${cd.nom} » ?")
                    .setPositiveButton("Supprimer") { _, _ -> stock.supprimer(cd.nom); montrerMesCadrans() }
                    .setNegativeButton("Garder", null)
                    .show()
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(8), dp(2), dp(16), dp(6)) })
            col.addView(l)
        }
        val d = ScrollView(this)
        d.addView(col)
        cadre(d)
    }

    // ---------- Marques ----------

    private fun montrerMarques() {
        val col = colonne()
        col.addView(barre("Marques de montres", "Marques"))
        col.addView(texte("Chaque marque s'ajoute comme un bloc. Voilà où on en est, sans rien promettre de plus.", 16f))
        Catalogue.marques.forEach {
            col.addView(texte(it.nom, 18f, gras = true))
            col.addView(texte(it.etat + " : " + it.detail, 15f, Color.DKGRAY))
        }
        val d = ScrollView(this)
        d.addView(col)
        cadre(d)
    }

    // ---------- Boutiques ----------

    private fun montrerBoutiques() {
        val col = colonne()
        col.addView(barre("Boutiques", "Boutiques"))
        col.addView(texte("Choisis où diffuser tes cadrans. Ton choix est mémorisé. L'envoi automatique n'est pas encore disponible.", 16f))
        Catalogue.boutiques.forEach { b ->
            col.addView(interrupteur(b.nom + " (" + b.pour + ")", stock.boutiqueChoisie(b.nom)) { stock.choisirBoutique(b.nom, it) })
            col.addView(texte(b.remarque, 14f, Color.GRAY))
        }
        col.addView(bouton("Publier") {
            AlertDialog.Builder(this)
                .setMessage("La publication n'est pas encore disponible dans cette version. Aucun envoi n'a été fait.")
                .setPositiveButton("OK", null)
                .show()
        }, margeBouton())
        val d = ScrollView(this)
        d.addView(col)
        cadre(d)
    }
}