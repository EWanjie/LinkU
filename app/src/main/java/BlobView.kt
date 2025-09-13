package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.Choreographer
import kotlin.math.*

class BlobView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), Choreographer.FrameCallback {

    var blobBackgroundColor: Int = Color.WHITE
    var startColor: Int = Color.rgb(30, 60, 200)
    var endColor: Int = Color.rgb(90, 0, 180)
    var showRings: Boolean = true
    var rings: Int = 2
    var speed: Float = 0.008f

    private var t = 0f
    private var baseR = 220f
    private var noiseScale = 1.1f
    private var wobbleBase = 28f
    private val pointsStep = 0.02f

    private val path = Path()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val glowPaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        color = Color.argb(80, 120, 60, 255)
        maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
        pathEffect = CornerPathEffect(40f)   // сглаживает углы
    }
    private val glowPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.argb(120, 40, 160, 255)
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        pathEffect = CornerPathEffect(40f)
    }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1.5f; color = Color.argb(20, 0, 0, 0)
    }

    private val clipPath = Path()
    private val pts = ArrayList<PointF>(ceil((2 * Math.PI / pointsStep).toFloat()).toInt())

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fillPaint.shader = LinearGradient(
            (w/2f)-baseR, (h/2f)-baseR, (w/2f)+baseR, (h/2f)+baseR,
            startColor, endColor, Shader.TileMode.CLAMP
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Choreographer.getInstance().postFrameCallback(this)
    }
    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(this)
        super.onDetachedFromWindow()
    }
    override fun doFrame(frameTimeNanos: Long) {
        t += speed
        invalidate()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(c: Canvas) {
        c.drawColor(blobBackgroundColor)

        val cx = width/2f; val cy = height/2f
        pts.clear()
        var a = 0f

        // мягче контур:
        val wobble = wobbleBase.coerceAtMost(28f)   // было 40
        val localNoiseScale = max(1.0f, noiseScale) // ~1.0..1.2 ок

        while (a < TWO_PI_F) {
            val nx = cos(a) * localNoiseScale + 10f
            val ny = sin(a) * localNoiseScale + 20f
            val n  = Simplex.noise3(nx.toDouble(), ny.toDouble(), t.toDouble()).toFloat()
            // убрали высокочастотную волну +sin(a*3f + t*2.2f)*8f
            val r  = baseR + lerp(-wobble, wobble, (n + 1f) * 0.5f)
            pts.add(PointF(cx + r * cos(a), cy + r * sin(a)))
            a += 0.015f                        // шаг меньше для гладкости (было pointsStep=0.02/0.03)
        }

        // СНАЧАЛА СТРОИМ PATH!
        path.rewind()
        if (pts.isNotEmpty()) {
            path.moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) path.lineTo(pts[i].x, pts[i].y)
            path.close()
        }

        // >>> АУРА (расширенное свечение) — здесь <<<
        c.drawPath(path, auraPaint)  // первый широкий слой
        // второй, ещё шире/мягче (необязательно):
        auraPaint.maskFilter = BlurMaskFilter(120f, BlurMaskFilter.Blur.NORMAL)
        auraPaint.alpha = 28
        c.drawPath(path, auraPaint)
        // вернуть базовые настройки для остального рендера
        auraPaint.maskFilter = BlurMaskFilter(64f, BlurMaskFilter.Blur.NORMAL)
        auraPaint.alpha = 55

        // clip + gradient fill
        c.save()
        clipPath.rewind(); clipPath.addPath(path); c.clipPath(clipPath)
        c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)

        // inner volume
        val alphas = intArrayOf(35, 27, 19)
        for (i in alphas.indices) {
            val rr = baseR*(0.65f - i*0.12f) + sin(t*1.4f + i)*6f
            innerPaint.alpha = alphas[i]
            c.drawCircle(cx, cy, rr, innerPaint)
        }

        // cores
        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL; color = Color.argb(220, 240, 255, 255)
        }
        for (i in 0 until 8) {
            val aa = t*(0.8f + i*0.05f) + i
            val bb = t*(1.3f - i*0.03f) + i*0.6f
            val x = cx + cos(aa)*(baseR*0.32f + sin(bb)*14f)
            val y = cy + sin(bb)*(baseR*0.28f + cos(aa)*10f)
            val d = 22f + 8f*sin(t*2f + i)
            c.drawCircle(x, y, d/2f, corePaint)
        }
        c.restore()

        // неон-обводки — добавь сглаживание углов
        glowPaint1.pathEffect = CornerPathEffect(40f)
        glowPaint2.pathEffect = CornerPathEffect(40f)
        c.drawPath(path, glowPaint1)
        c.drawPath(path, glowPaint2)

        // кольца
        if (showRings && rings > 0) {
            c.save(); c.translate(cx, cy)
            var printed = 0; var r = 270f
            while (printed < rings) {
                val d = r + sin(t*2f)*6f
                c.drawOval(RectF(-d/2f, -d/2f, d/2f, d/2f), ringPaint)
                r += 30f; printed++
            }
            c.restore()
        }
    }

    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(55, 90, 110, 255)           // мягкий сине-фиолетовый
        maskFilter = BlurMaskFilter(64f, BlurMaskFilter.Blur.NORMAL) // радиус свечения
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    companion object { private const val TWO_PI_F = (Math.PI * 2).toFloat() }
}

/* Simplex (минимум для noise3) */
private object Simplex {
    private const val STRETCH_3D = -1.0 / 6
    private const val SQUISH_3D = 1.0 / 3
    private const val NORM_3D = 103

    // градиенты ОБЪЯВЛЕНЫ до init
    private val gradients3D = doubleArrayOf(
        2.22474487139, 2.22474487139, -1.0,  -2.22474487139, 2.22474487139, -1.0,
        2.22474487139, -2.22474487139, -1.0, -2.22474487139, -2.22474487139, -1.0,
        2.22474487139, 2.22474487139, 1.0,   -2.22474487139, 2.22474487139, 1.0,
        2.22474487139, -2.22474487139, 1.0,  -2.22474487139, -2.22474487139, 1.0,
        1.0, 2.22474487139, 2.22474487139,   -1.0, 2.22474487139, 2.22474487139,
        1.0, -2.22474487139, 2.22474487139,  -1.0, -2.22474487139, 2.22474487139,
        1.0, 2.22474487139, -2.22474487139,  -1.0, 2.22474487139, -2.22474487139,
        1.0, -2.22474487139, -2.22474487139, -1.0, -2.22474487139, -2.22474487139
    )

    private val perm = IntArray(256) { it }.apply { shuffle() } + IntArray(256) { 0 }
    private val permGradIndex3D = IntArray(512)

    init {
        for (i in 0 until 512) {
            val idx = perm[i and 255]
            permGradIndex3D[i] = (idx % (gradients3D.size / 3)) * 3
        }
    }

    fun noise3(x: Double, y: Double, z: Double): Double {
        fun fastFloor(v: Double) = if (v >= 0) v.toInt() else v.toInt() - 1
        val stretchOffset = (x + y + z) * STRETCH_3D
        val xs = x + stretchOffset; val ys = y + stretchOffset; val zs = z + stretchOffset
        val xsb = fastFloor(xs); val ysb = fastFloor(ys); val zsb = fastFloor(zs)
        val squish = (xsb + ysb + zsb) * SQUISH_3D
        val dx0 = x - (xsb + squish); val dy0 = y - (ysb + squish); val dz0 = z - (zsb + squish)
        var value = 0.0

        fun contrib(xi: Int, yi: Int, zi: Int, dx: Double, dy: Double, dz: Double) {
            var attn = 2.0 - dx*dx - dy*dy - dz*dz
            if (attn > 0) {
                val px = (xi + xsb) and 255; val py = (yi + ysb) and 255; val pz = (zi + zsb) and 255
                val gi = permGradIndex3D[(perm[px] + py) and 255] + (pz % (gradients3D.size/3))*0
                val gIndex = (perm[px] + py + pz) and 255
                val gi3 = (gIndex % (gradients3D.size / 3)) * 3
                val gx = gradients3D[gi3]; val gy = gradients3D[gi3 + 1]; val gz = gradients3D[gi3 + 2]
                val dot = gx*dx + gy*dy + gz*dz
                attn *= attn; value += attn*attn*dot
            }
        }
        contrib(0,0,0, dx0,dy0,dz0); contrib(1,0,0, dx0-1,dy0,dz0); contrib(0,1,0, dx0,dy0-1,dz0)
        contrib(0,0,1, dx0,dy0,dz0-1); contrib(1,1,0, dx0-1,dy0-1,dz0)
        contrib(1,0,1, dx0-1,dy0,dz0-1); contrib(0,1,1, dx0,dy0-1,dz0-1); contrib(1,1,1, dx0-1,dy0-1,dz0-1)
        return value / NORM_3D
    }

    private fun IntArray.shuffle() {
        for (i in indices.reversed()) {
            val j = (Math.random()*(i+1)).toInt()
            val t = this[i]; this[i] = this[j]; this[j] = t
        }
    }
}