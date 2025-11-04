package com.bodyparts.vitals.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.bodyparts.vitals.model.BodyPart

class BodyPartsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bodyParts = mutableListOf<BodyPart>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val normalColor = Color.parseColor("#E0E0E0")
    private val selectedColor = Color.RED
    private val strokeColor = Color.BLACK

    var onBodyPartSelectedListener: ((BodyPart) -> Unit)? = null

    init {
        paint.style = Paint.Style.FILL
        textPaint.color = Color.BLACK
        textPaint.textSize = 32f
        textPaint.textAlign = Paint.Align.CENTER

        // Initialize body parts with their regions
        initializeBodyParts()
    }

    private fun initializeBodyParts() {
        // Define body part regions as percentages of view dimensions
        // These will be calculated in onSizeChanged()
        bodyParts.clear()
        bodyParts.addAll(
            listOf(
                BodyPart("head", "Head", RectF()),
                BodyPart("chest", "Chest", RectF()),
                BodyPart("abdomen", "Abdomen", RectF()),
                BodyPart("left_arm", "Left Arm", RectF()),
                BodyPart("right_arm", "Right Arm", RectF()),
                BodyPart("left_leg", "Left Leg", RectF()),
                BodyPart("right_leg", "Right Leg", RectF())
            )
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val centerX = w / 2f
        val bodyWidth = w * 0.3f
        val headRadius = w * 0.1f

        // Define regions for each body part
        bodyParts.find { it.id == "head" }?.region?.set(
            centerX - headRadius,
            h * 0.05f,
            centerX + headRadius,
            h * 0.15f
        )

        bodyParts.find { it.id == "chest" }?.region?.set(
            centerX - bodyWidth / 2,
            h * 0.18f,
            centerX + bodyWidth / 2,
            h * 0.35f
        )

        bodyParts.find { it.id == "abdomen" }?.region?.set(
            centerX - bodyWidth / 2,
            h * 0.35f,
            centerX + bodyWidth / 2,
            h * 0.50f
        )

        bodyParts.find { it.id == "left_arm" }?.region?.set(
            centerX - bodyWidth / 2 - bodyWidth * 0.4f,
            h * 0.18f,
            centerX - bodyWidth / 2,
            h * 0.45f
        )

        bodyParts.find { it.id == "right_arm" }?.region?.set(
            centerX + bodyWidth / 2,
            h * 0.18f,
            centerX + bodyWidth / 2 + bodyWidth * 0.4f,
            h * 0.45f
        )

        bodyParts.find { it.id == "left_leg" }?.region?.set(
            centerX - bodyWidth / 2,
            h * 0.50f,
            centerX - bodyWidth * 0.1f,
            h * 0.85f
        )

        bodyParts.find { it.id == "right_leg" }?.region?.set(
            centerX + bodyWidth * 0.1f,
            h * 0.50f,
            centerX + bodyWidth / 2,
            h * 0.85f
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (bodyPart in bodyParts) {
            // Draw body part
            paint.color = if (bodyPart.isSelected) selectedColor else normalColor
            paint.style = Paint.Style.FILL

            if (bodyPart.id == "head") {
                // Draw head as oval
                canvas.drawOval(bodyPart.region, paint)
            } else {
                // Draw other parts as rounded rectangles
                canvas.drawRoundRect(bodyPart.region, 20f, 20f, paint)
            }

            // Draw border
            paint.color = strokeColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f

            if (bodyPart.id == "head") {
                canvas.drawOval(bodyPart.region, paint)
            } else {
                canvas.drawRoundRect(bodyPart.region, 20f, 20f, paint)
            }

            // Draw label
            val centerX = bodyPart.region.centerX()
            val centerY = bodyPart.region.centerY()

            // Adjust text size based on region size
            textPaint.textSize = minOf(bodyPart.region.width(), bodyPart.region.height()) * 0.2f

            canvas.drawText(
                bodyPart.name,
                centerX,
                centerY + textPaint.textSize / 3,
                textPaint
            )
        }

        paint.style = Paint.Style.FILL
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            for (bodyPart in bodyParts) {
                if (bodyPart.region.contains(x, y)) {
                    bodyPart.isSelected = !bodyPart.isSelected
                    onBodyPartSelectedListener?.invoke(bodyPart)
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun getSelectedBodyParts(): List<BodyPart> {
        return bodyParts.filter { it.isSelected }
    }

    fun getBodyParts(): List<BodyPart> {
        return bodyParts
    }

    fun clearSelections() {
        bodyParts.forEach { it.isSelected = false }
        invalidate()
    }
}
