package fr.cadrans

import android.app.Activity
import android.graphics.Bitmap
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/** Parcours réel de l'appli sur l'appareil : on touche les boutons, on prend des captures, on vérifie qu'aucune page n'est une impasse. */
@RunWith(AndroidJUnit4::class)
class ParcoursTest {
    private val inst = InstrumentationRegistry.getInstrumentation()
    private lateinit var scenario: ActivityScenario<MainActivity>
    private var activite: Activity? = null

    @Before fun avant() {
        inst.targetContext.getSharedPreferences("cadrans", 0).edit().clear().commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activite = it }
        inst.waitForIdleSync()
    }

    @After fun apres() { scenario.close() }

    private fun dossier(): File = File(inst.targetContext.getExternalFilesDir(null), "captures").apply { mkdirs() }

    private fun capture(nom: String) {
        inst.waitForIdleSync()
        Thread.sleep(400)
        val b = inst.uiAutomation.takeScreenshot() ?: return
        FileOutputStream(File(dossier(), "$nom.png")).use { b.compress(Bitmap.CompressFormat.PNG, 90, it) }
    }

    private fun trouver(v: View, texte: String): View? {
        if (v is TextView && v.text.toString() == texte && v.isShown) return v
        if (v is ViewGroup) for (i in 0 until v.childCount) trouver(v.getChildAt(i), texte)?.let { return it }
        return null
    }

    private fun present(texte: String): Boolean {
        var r = false
        inst.runOnMainSync { r = trouver(activite!!.window.decorView, texte) != null }
        return r
    }

    private fun cliquer(texte: String) {
        var ok = false
        inst.runOnMainSync {
            val v = trouver(activite!!.window.decorView, texte)
            if (v != null) { v.performClick(); ok = true }
        }
        assertTrue("Bouton introuvable : $texte", ok)
        inst.waitForIdleSync()
    }

    private fun toucheRetourAndroid() {
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        inst.waitForIdleSync()
    }

    @Test fun accueilPuisChaqueEcranAvecRetour() {
        assertTrue(present("Créer un cadran"))
        capture("01-accueil")
        for (ecran in listOf("Mes cadrans", "Marques de montres", "Boutiques (diffusion)")) {
            cliquer(ecran)
            assertTrue("Pas de bouton Retour sur $ecran", present("← Retour"))
            assertTrue("Pas de bouton d'aide sur $ecran", present("?"))
            capture("02-" + ecran.take(6).replace(' ', '-'))
            cliquer("← Retour")
            assertTrue("Retour cassé depuis $ecran", present("Créer un cadran"))
        }
    }

    @Test fun editeurChoixEtRetourAndroid() {
        cliquer("Créer un cadran")
        assertTrue(present("1. Choisis un modèle"))
        assertTrue(present("← Retour"))
        capture("10-editeur-classique")
        cliquer("Sport"); capture("11-editeur-sport")
        cliquer("Carrée"); capture("12-editeur-carree")
        cliquer("Chiffres"); capture("13-editeur-chiffres")
        cliquer("Les deux"); capture("14-editeur-mixte")
        cliquer("Rétro"); capture("15-editeur-retro")
        cliquer("Plus d'options")
        assertTrue(present("Heure sur 24 h (sinon 12 h)"))
        capture("16-plus-d-options")
        toucheRetourAndroid()
        assertTrue("Le geste retour doit ramener à l'accueil", present("Créer un cadran"))
    }

    @Test fun enregistrerPuisRetrouver() {
        cliquer("Créer un cadran")
        cliquer("Enfant")
        cliquer("Enregistrer mon cadran")
        toucheRetourAndroid() // ferme la fenêtre de confirmation
        cliquer("← Retour")
        cliquer("Mes cadrans")
        assertTrue("Le cadran enregistré doit apparaître", present("Enfant"))
        capture("20-mes-cadrans")
        cliquer("Ouvrir")
        assertTrue(present("1. Choisis un modèle"))
        capture("21-cadran-rouvert")
    }

    @Test fun boutiquePuisPublierEstHonnete() {
        cliquer("Boutiques (diffusion)")
        cliquer("Publier")
        toucheRetourAndroid()
        assertNotNull(activite)
        assertTrue(present("← Retour"))
    }
}