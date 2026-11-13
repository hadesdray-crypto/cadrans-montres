package fr.cadrans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Dessine un cadran. Sert à l'aperçu de la montre virtuelle (et servira à fabriquer l'image d'aperçu des boutiques). */
class RenduCadran {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val t = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val zone = RectF()

    private fun yTexte(centreY: Float): Float = centreY - (t.ascent() + t.descent()) / 2f

    fun dessiner(c: Canvas, w: Float, h: Float, cadran: Cadran, cal: Calendar, batterie: Int = 78, pas: Int = 6432) {
        val cx = w / 2f
        val cy = h / 2f
        val r = min(w, h) / 2f
        val rf = r * 0.93f

        // Boîtier
        p.style = Paint.Style.FILL
        p.color = 0xFF2B2B2B.toInt()
        if (cadran.forme == Forme.ROND) c.drawCircle(cx, cy, r, p) else {
            zone.set(cx - r, cy - r, cx + r, cy + r); c.drawRoundRect(zone, r * 0.3f, r * 0.3f, p)
        }
        // Fond
        p.color = cadran.couleurFond
        if (cadran.forme == Forme.ROND) c.drawCircle(cx, cy, rf, p) else {
            zone.set(cx - rf, cy - rf, cx + rf, cy + rf); c.drawRoundRect(zone, rf * 0.26f, rf * 0.26f, p)
        }

        val analogique = cadran.affichage != Affichage.NUMERIQUE
        val rt = rf * 0.92f

        // Graduations
        if (cadran.graduations && analogique) {
            for (i in 0 until 60) {
                val grand = i % 5 == 0
                p.color = if (grand) cadran.couleurAccent else (cadran.couleurTexte and 0x00FFFFFF) or 0x88000000.toInt()
                p.strokeWidth = if (grand) rf * 0.025f else rf * 0.01f
                p.strokeCap = Paint.Cap.ROUND
                val a = Math.toRadians(i * 6.0)
                val l = if (grand) rf * 0.09f else rf * 0.04f
                c.drawLine(
                    cx + (sin(a) * rt).toFloat(), cy - (cos(a) * rt).toFloat(),
                    cx + (sin(a) * (rt - l)).toFloat(), cy - (cos(a) * (rt - l)).toFloat(), p
                )
            }
        }

        // Chiffres des heures (seulement si on a des aiguilles sans heure numérique)
        if (cadran.chiffres && cadran.affichage == Affichage.ANALOGIQUE) {
            t.color = cadran.couleurTexte
            t.textSize = rf * 0.15f
            for (n in 1..12) {
                val a = Math.toRadians(n * 30.0)
                val rn = rt * 0.76f
                c.drawText(n.toString(), cx + (sin(a) * rn).toFloat(), yTexte(cy - (cos(a) * rn).toFloat()), t)
            }
        }

        // Heure numérique et lignes d'informations
        val lignes = DateFrancaise.lignes(cadran, cal, batterie, pas)
        val tailleLigne = rf * 0.11f
        t.color = cadran.couleurTexte
        t.textSize = tailleLigne
        when (cadran.affichage) {
            Affichage.ANALOGIQUE -> lignes.forEachIndexed { i, s -> c.drawText(s, cx, yTexte(cy + rf * 0.30f + i * rf * 0.15f), t) }
            Affichage.NUMERIQUE -> {
                t.color = cadran.couleurAccent
                t.textSize = rf * if (cadran.secondes) 0.30f else 0.40f
                c.drawText(DateFrancaise.heure(cal, cadran.format24h, cadran.secondes), cx, yTexte(cy - rf * 0.12f), t)
                t.color = cadran.couleurTexte
                t.textSize = tailleLigne
                lignes.forEachIndexed { i, s -> c.drawText(s, cx, yTexte(cy + rf * 0.22f + i * rf * 0.15f), t) }
            }
            Affichage.MIXTE -> {
                t.color = cadran.couleurAccent
                t.textSize = rf * 0.20f
                c.drawText(DateFrancaise.heure(cal, cadran.format24h, false), cx, yTexte(cy - rf * 0.42f), t)
                t.color = cadran.couleurTexte
                t.textSize = tailleLigne
                lignes.forEachIndexed { i, s -> c.drawText(s, cx, yTexte(cy + rf * 0.42f + i * rf * 0.14f), t) }
            }
        }

        // Aiguilles
        if (analogique) {
            val h12 = cal.get(Calendar.HOUR) + cal.get(Calendar.MINUTE) / 60f
            val m = cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) / 60f
            val s = cal.get(Calendar.SECOND).toFloat()
            aiguille(c, cx, cy, h12 * 30f, rf * 0.50f, rf * 0.05f, cadran.couleurAiguilles)
            aiguille(c, cx, cy, m * 6f, rf * 0.74f, rf * 0.035f, cadran.couleurAiguilles)
            if (cadran.secondes) aiguille(c, cx, cy, s * 6f, rf * 0.82f, rf * 0.012f, cadran.couleurAccent)
            p.style = Paint.Style.FILL
            p.color = cadran.couleurAccent
            c.drawCircle(cx, cy, rf * 0.045f, p)
        }
    }

    private fun aiguille(c: Canvas, cx: Float, cy: Float, degres: Float, longueur: Float, epaisseur: Float, couleur: Int) {
        c.save()
        c.rotate(degres, cx, cy)
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        p.strokeWidth = epaisseur
        p.color = couleur
        c.drawLine(cx, cy + longueur * 0.15f, cx, cy - longueur, p)
        c.restore()
    }
}