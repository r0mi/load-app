package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0f
    private var heightSize = 0f

    private var progress = 0f
    private var sweepAngle = 0f
    private var buttonWidth = 0f
    private var circleLeft = 0f
    private var circleTop = 0f
    private var circleRight = 0f
    private var circleBottom = 0f
    private var textLeft = 0f
    private var textTop = 0f

    private var buttonTextColor = 0
    private var buttonBackgroundColor = 0
    private var buttonLoadingBackgroundColor = 0
    private var buttonLoadingCircleColor = 0
    private var buttonTextSize = 0
    private var currentDuration = 2500L
    private var decayFraction = 0f
    private var stop = false

    private var buttonTextBounds = Rect()
    private val valueAnimator = ValueAnimator()

    private val buttonTextDownload = resources.getString(R.string.button_download)
    private val buttonTextLoading = resources.getString(R.string.button_loading)
    private var buttonText = buttonTextDownload

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        buttonText = when (new) {
            ButtonState.Loading -> buttonTextLoading
            else -> buttonTextDownload
        }
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonTextColor = getColor(R.styleable.LoadingButton_buttonTextColor, 0)
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            buttonLoadingBackgroundColor =
                getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            buttonLoadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, 0)
            buttonTextSize = getDimensionPixelSize(R.styleable.LoadingButton_textSize, 0)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = buttonTextSize.toFloat()
        typeface = Typeface.create("", Typeface.NORMAL)
    }

    override fun performClick(): Boolean {
        when (buttonState) {
            ButtonState.Completed -> {
                buttonState = ButtonState.Clicked
            }
            else -> {
            }
        }

        super.performClick()

        updateValues()
        invalidate()

        return true
    }

    private fun showLoading(repeatCount: Int) {
        stop = false
        valueAnimator.removeAllUpdateListeners()
        valueAnimator.apply {
            setFloatValues(0f, 1f)
            duration = currentDuration
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                progress = animation.animatedValue as Float
                animation.repeatCount = repeatCount
                animation.repeatMode = ValueAnimator.RESTART
                updateValues()
                if (!stop && animation.repeatCount != 0 && animation.animatedFraction >= decayFraction) {
                    slowDownLoading(animation.animatedFraction)
                } else {
                    invalidate()
                }
            }
            doOnStart {
                buttonState = ButtonState.Loading
                isEnabled = false
            }
            doOnEnd {
                buttonState = ButtonState.Completed
                isEnabled = true
                progress = 0f
            }
            start()
        }
    }

    private fun slowDownLoading(currentFraction: Float) {
        decayFraction = currentFraction * 1.1f
        currentDuration += 1000
        valueAnimator.apply {
            setFloatValues(progress, 1f)
            duration = currentDuration
            start()
        }
    }

    private fun stopLoading() {
        valueAnimator.apply {
            setFloatValues(progress, 1f)
            duration = 500
            interpolator = AccelerateInterpolator()
            addUpdateListener { animation ->
                progress = animation.animatedValue as Float
                animation.repeatCount = 0
                animation.repeatMode = ValueAnimator.RESTART
                invalidate()
            }
            start()
        }
    }

    fun finish() {
        stop = true
        stopLoading()
    }

    fun startIndefinite() {
        decayFraction = 0.1f
        currentDuration = 1000
        showLoading(ValueAnimator.INFINITE)
    }

    fun startOnce() {
        currentDuration = 1000
        decayFraction = 1.0f
        showLoading(0)
    }

    private fun updateValues() {
        paint.getTextBounds(buttonText, 0, buttonText.length, buttonTextBounds)
        buttonWidth = widthSize * progress
        sweepAngle = 360f * progress
        circleLeft = widthSize / 2f + buttonTextBounds.width() / 2f + buttonTextSize / 2f
        circleTop = heightSize / 2f - buttonTextSize / 2f
        circleRight = circleLeft + buttonTextSize
        circleBottom = circleTop + buttonTextSize
        textLeft = widthSize / 2f
        textTop = height / 2f - (paint.ascent() + paint.descent()) / 2
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateValues()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(buttonBackgroundColor)

        if (buttonState == ButtonState.Loading) {
            paint.color = buttonLoadingBackgroundColor
            canvas.drawRect(0f, 0f, buttonWidth, heightSize, paint)

            paint.color = buttonLoadingCircleColor
            canvas.drawArc(
                circleLeft,
                circleTop,
                circleRight,
                circleBottom,
                0f,
                sweepAngle,
                true,
                paint
            )
        }

        paint.color = buttonTextColor
        canvas.drawText(buttonText, textLeft, textTop, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w.toFloat()
        heightSize = h.toFloat()
        setMeasuredDimension(w, h)
    }

}