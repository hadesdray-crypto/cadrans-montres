package fr.cadrans

import android.content.Context
import android.graphics.Canvas
import android.view.View
import java.util.Calendar

/** La montre virtuelle : montre le cadran en direct, avec l'heure réelle ou une heure d'essai. */
class MontreVirtuelleView(context: Context) : View(context) {
    var cadran: Cadran = Modeles.liste[0]
        set(v) { field = v; invalidate() }

    /** Heure d'essai en minutes depuis minuit ; null = heure réelle. */
    var minutesEssai: Int? = null
        set(v) { field = v; invalidate() }

    private val rendu = RenduCadran()
    private val tic = object : Runnable {
        override fun run() { invalidate(); postDelayed(this, 1000) }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); post(tic) }
    override fun onDetachedFromWindow() { removeCallbacks(tic); super.onDetachedFromWindow() }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, w)
    }

    override fun onDraw(canvas: Canvas) {
        val cal = Calendar.getInstance()
        minutesEssai?.let {
            cal.set(Calendar.HOUR_OF_DAY, it / 60)
            cal.set(Calendar.MINUTE, it % 60)
            cal.set(Calendar.SECOND, 0)
        }
        rendu.dessiner(canvas, width.toFloat(), height.toFloat(), cadran, cal)
    }
}