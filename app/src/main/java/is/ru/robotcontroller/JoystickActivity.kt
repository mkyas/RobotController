package `is`.ru.robotcontroller

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class JoystickActivity : SurfaceView, SurfaceHolder.Callback, View.OnTouchListener {

        constructor(context: Context): super(context) {
            setupJoystickView()
            initWithoutAttrs()
        }

        constructor(context: Context, attributes: AttributeSet): super(context, attributes) {
            setupJoystickView()
            initAttributes(context, attributes)
        }

        constructor(context: Context, attributes: AttributeSet, style: Int) : super(context, attributes) {
            setupJoystickView()
            initAttributes(context, attributes)
        }

        private var centerX: Float = 0F
        private var centerY: Float = 0F

        private var baseRadius: Float = 0F
        private var hatRadius: Float = 0F

        var baseColor: Int = "#303F9F".toColorInt()
        private var baseA: Int = 0
        private var baseR: Int = 0
        private var baseG: Int = 0
        private var baseB: Int = 0

        var hatColor: Int = "#5E5E92".toColorInt()
        private var hatA: Int = 0
        private var hatR: Int = 0
        private var hatG: Int = 0
        private var hatB: Int = 0

        var stickShadeColor: Int = "#afffff".toColorInt()
        private var stickShadeR: Int = 0
        private var stickShadeG: Int = 0
        private var stickShadeB: Int = 0

        var drawBase: Boolean = true
        var drawStick: Boolean = true
        var shadeHat: Boolean = true

        var ratio: Int = 5

        private lateinit var joystickListener: JoyStickListener

        private fun setupDimensions() {
            centerX = (width / 2).toFloat()
            centerY = (height / 2).toFloat()
            baseRadius = min(width * 0.93F, height * 0.93F) / 3
            hatRadius = min(width * 0.93F, height * 0.93F) / 5
        }

        private fun setupJoystickView() {
            holder.addCallback(this)
            setOnTouchListener(this)
            setBackgroundColor(Color.TRANSPARENT)
            setZOrderOnTop(true)
            holder.setFormat(PixelFormat.TRANSPARENT)
        }

        private fun initAttributes(context: Context, attrs: AttributeSet) {
            context.withStyledAttributes(attrs, R.styleable.JoystickActivity) {
                val base = getColor(R.styleable.JoystickActivity_base_color, "#303F9F".toColorInt())
                val hat = getColor(R.styleable.JoystickActivity_hat_color, "#5E5E92".toColorInt())
                val stickShade =
                    getColor(R.styleable.JoystickActivity_stick_shade_color, "#afffff".toColorInt())

                // Conversion from int to ARGB value
                baseA = base shr 24 and 0xff
                baseR = base shr 16 and 0xff
                baseG = base shr 8 and 0xff
                baseB = base and 0xff

                hatA = hat shr 24 and 0xff
                hatR = hat shr 16 and 0xff
                hatG = hat shr 8 and 0xff
                hatB = hat and 0xff

                stickShadeR = stickShade shr 16 and 0xff
                stickShadeG = stickShade shr 8 and 0xff
                stickShadeB = stickShade and 0xff

                ratio = getInteger(R.styleable.JoystickActivity_ratio, 5)
                drawStick = getBoolean(R.styleable.JoystickActivity_draw_stick_shading, true)
                shadeHat = getBoolean(R.styleable.JoystickActivity_draw_hat_shading, true)
                drawBase = getBoolean(R.styleable.JoystickActivity_draw_base, true)

            }
        }

        private fun initWithoutAttrs() {
            // Conversion from int to ARGB value
            baseA = baseColor shr 24 and 0xff
            baseR = baseColor shr 16 and 0xff
            baseG = baseColor shr 8 and 0xff
            baseB = baseColor and 0xff

            hatA = hatColor shr 24 and 0xff
            hatR = hatColor shr 16 and 0xff
            hatG = hatColor shr 8 and 0xff
            hatB = hatColor and 0xff

            stickShadeR = stickShadeColor shr 16 and 0xff
            stickShadeG = stickShadeColor shr 8 and 0xff
            stickShadeB = stickShadeColor and 0xff
        }

        private fun drawJoystick(newX: Float, newY: Float) {
            if(holder.surface.isValid) {
                val myCanvas = this.holder.lockCanvas()
                val colors = Paint()
                myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                val hypotenuse: Float = sqrt((newX - centerX).pow(2) + (newY - centerY).pow(2))
                val sin: Float = (newY - centerY) / hypotenuse
                val cos: Float = (newX - centerX) / hypotenuse

                if(drawBase) {
                    colors.setARGB(baseA, baseR, baseG, baseB)
                    myCanvas.drawCircle(centerX, centerY, baseRadius, colors)
                }

                if(drawStick) {
                    for(i in (1..(baseRadius/ratio).toInt())) {
                        colors.setARGB(150 / i, stickShadeR, stickShadeG, stickShadeB)
                        myCanvas.drawCircle(newX - cos * hypotenuse * (ratio / baseRadius) * i,
                            newY - sin * hypotenuse * (ratio / baseRadius) * i,
                            i * (hatRadius * ratio / baseRadius), colors)
                    }
                }

                if(shadeHat) {
                    val numLoops: Int = (hatR / ratio)
                    val rChange: Int = (255 - hatR) / numLoops
                    val gChange: Int = (255 - hatG) / numLoops
                    val bChange: Int = (255 - hatB) / numLoops

                    for(i in (0..(hatRadius / ratio).toInt())) {
                        colors.setARGB(255, hatR + (i * rChange),
                            hatG + (i * gChange), hatB + (i * bChange))
                        myCanvas.drawCircle(newX, newY, hatRadius - i.toFloat() * (ratio) / 2, colors)
                    }
                }
                else {
                    colors.setARGB(hatA, hatR, hatG, hatB)
                    myCanvas.drawCircle(newX, newY, hatRadius, colors)
                }

                holder.unlockCanvasAndPost(myCanvas)
            }
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            setupDimensions()
            drawJoystick(centerX, centerY)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {

        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if(v?.equals(this) == true) {
                if(event?.action != MotionEvent.ACTION_UP) {
                    val displacement: Float =
                        sqrt((event!!.x - centerX).pow(2) + (event.y - centerY).pow(2))
                    if(displacement < baseRadius) {
                        drawJoystick(event.x, event.y)
                        updateListener((event.x - centerX)/baseRadius, (event.y - centerY)/baseRadius)
                    }
                    else {
                        val ratio: Float = baseRadius / displacement
                        val constrainedX: Float = centerX + (event.x - centerX) * ratio
                        val constrainedY: Float = centerY + (event.y - centerY) * ratio
                        drawJoystick(constrainedX, constrainedY)
                        updateListener((constrainedX-centerX)/baseRadius, (constrainedY-centerY)/baseRadius)
                    }
                }
                else {
                    drawJoystick(centerX, centerY)
                    updateListener((0).toFloat(),(0).toFloat())
                }
            }
            return true
        }

        private fun updateListener(xPercent: Float, yPercent: Float) {
            joystickListener.onJoystickMoved(xPercent, yPercent, id)
        }

        fun setJoystickListener(joystickListener: JoyStickListener) {
            this.joystickListener = joystickListener
        }

        interface JoyStickListener {
            fun onJoystickMoved(xPercent: Float, yPercent: Float, id: Int)
        }
    }