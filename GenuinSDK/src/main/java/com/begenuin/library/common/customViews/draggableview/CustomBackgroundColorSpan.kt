package com.begenuin.begenuin.ui.customview.draggableview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.style.LineBackgroundSpan
import kotlin.math.abs
import kotlin.math.sign

//https://stackoverflow.com/questions/48096722/rounded-background-text-like-instagram-replacementspan-not-working-as-required
class CustomBackgroundColorSpan(
    private val backgroundColor: Int,
    private val padding: Int,
    private val radius: Int,
) : LineBackgroundSpan {
    private val rect = RectF()
    private val paint = Paint()
    private val paintStroke = Paint()
    private val path = Path()
    private var prevWidth = -1f
    private var prevLeft = -1f
    private var prevRight = -1f
    private var prevBottom = -1f
    private var prevTop = -1f

    private val ALIGN_CENTER = 0
    private val ALIGN_START = 1
    private val ALIGN_END = 2


    init {
        paint.color = backgroundColor
        paintStroke.color = backgroundColor
    }

    companion object {
        val ALIGN_START = 1
        val ALIGN_END = 2
        val ALIGN_CENTER = 3
    }

    private var align = ALIGN_CENTER

    fun setAlignment(alignment: Int) {
        align = alignment
    }

    fun getAlignment(): Int {
        return align
    }

    fun getAlignStart(): Int {
        return ALIGN_START
    }

    fun getAlignEnd(): Int {
        return ALIGN_END
    }

    fun getAlignCenter(): Int {
        return ALIGN_CENTER
    }


    override fun drawBackground(
        canvas1: Canvas,
        p: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int,
    ) {

        val width = p.measureText(text, start, end) + 2f * padding
        val shiftLeft: Float
        val shiftRight: Float

        //Alignmet set
        when (align) {
            ALIGN_START -> {
                shiftLeft = 0f - padding
                shiftRight = width + shiftLeft
            }

            ALIGN_END -> {
                shiftLeft = right - width + padding
                shiftRight = (right + padding).toFloat()
            }
            else -> {
                shiftLeft = (right - width) / 2
                shiftRight = right - shiftLeft
            }
        }

        rect.set(shiftLeft, top.toFloat(), shiftRight, bottom.toFloat())


        if (lnum == 0) {
            canvas1.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paint)
        } else {
            path.reset()
            val difference = width - prevWidth
            val diff = -sign(difference) * (2f * radius).coerceAtMost(abs(difference / 2f)) / 2f
            path.moveTo(
                prevLeft, prevBottom - radius
            )

            if (align != ALIGN_START) {
                path.cubicTo(//1
                    prevLeft, prevBottom - radius,
                    prevLeft, rect.top,
                    prevLeft + diff, rect.top
                )
            } else {
                path.lineTo(prevLeft, prevBottom + radius)
            }
            path.lineTo(
                rect.left - diff, rect.top
            )
            path.cubicTo(//2
                rect.left - diff, rect.top,
                rect.left, rect.top,
                rect.left, rect.top + radius
            )
            path.lineTo(
                rect.left, rect.bottom - radius
            )
            path.cubicTo(//3
                rect.left, rect.bottom - radius,
                rect.left, rect.bottom,
                rect.left + radius, rect.bottom
            )
            path.lineTo(
                rect.right - radius, rect.bottom
            )
            path.cubicTo(//4
                rect.right - radius, rect.bottom,
                rect.right, rect.bottom,
                rect.right, rect.bottom - radius
            )
            path.lineTo(
                rect.right, rect.top + radius
            )

            if (align != ALIGN_END) {
                path.cubicTo(//5
                    rect.right, rect.top + radius,
                    rect.right, rect.top,
                    rect.right + diff, rect.top
                )
                path.lineTo(
                    prevRight - diff, rect.top
                )
                path.cubicTo(//6
                    prevRight - diff, rect.top,
                    prevRight, rect.top,
                    prevRight, prevBottom - radius
                )

            } else {
                path.lineTo(prevRight, prevBottom - radius)
            }
            path.cubicTo(//7
                prevRight, prevBottom - radius,
                prevRight, prevBottom,
                prevRight - radius, prevBottom
            )

            path.lineTo(
                prevLeft + radius, prevBottom
            )

            path.cubicTo(//8
                prevLeft + radius, prevBottom,
                prevLeft, prevBottom,
                prevLeft, rect.top - radius
            )
            canvas1.drawPath(path, paintStroke)

        }
        prevWidth = width
        prevLeft = rect.left
        prevRight = rect.right
        prevBottom = rect.bottom
        prevTop = rect.top
    }
}