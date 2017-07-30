package com.mx.dxinl.pageindicator

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View

/**
 * Created by dxinl on 2017/07/30.
 *
 * Indicator for ViewPager
 */
class PageIndicator @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    enum class Orientation {
        VERTICAL, HORIZONTAL;
    }

    private lateinit var pager: ViewPager
    private val listener: ViewPager.OnPageChangeListener
    private val observer: DataSetObserver
    private val paint: Paint

    private var orientation = Orientation.HORIZONTAL
        set(value) {
            field.dupicateAssign(value) ?: return
            field = value
            requestLayout()
        }

    private var radius: Float = 0F
    private var dividerWidth: Float
    private var selectedColor: Int
    private var normalColor: Int
    private var centerX: Float = 0F
    private var centerY: Float = 0F
    private var startX: Float = 0F
    private var startY: Float = 0F
    private var position: Int = 0
    private var positionOffset: Float = 0F

    private var showInSinglePage: Boolean = true

    init {
        if (attrs != null) {
            val typedArray = context.theme
                    .obtainStyledAttributes(attrs, R.styleable.PageIndicator, defStyleAttr, 0)
            radius = typedArray.getDimension(R.styleable.PageIndicator_radius,
                    resources.getDimension(R.dimen.default_radius))
            dividerWidth = typedArray.getDimension(R.styleable.PageIndicator_dividerWidth,
                    resources.getDimension(R.dimen.default_divider_width))
            selectedColor = typedArray.getColor(R.styleable.PageIndicator_selectedColor,
                    resources.getColor(R.color.default_selected_color, context.theme))
            normalColor = typedArray.getColor(R.styleable.PageIndicator_normalColor,
                    resources.getColor(R.color.default_normal_color, context.theme))
            val orientationVal = typedArray.getInt(R.styleable.PageIndicator_orientation, 0);
            if (orientationVal == 0) {
                orientation = Orientation.HORIZONTAL
            } else {
                orientation = Orientation.VERTICAL
            }
            typedArray.recycle()
        } else {
            radius = resources.getDimension(R.dimen.default_radius)
            dividerWidth = resources.getDimension(R.dimen.default_divider_width)
            selectedColor = resources.getColor(R.color.default_selected_color, context.theme)
            normalColor = resources.getColor(R.color.default_normal_color, context.theme)
        }

        listener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                this@PageIndicator.position = position
                this@PageIndicator.positionOffset = positionOffset
                this@PageIndicator.invalidate()
            }

            override fun onPageSelected(position: Int) {
                onPageScrolled(position, 0F, 0)
            }
        }

        observer = object : DataSetObserver() {
            override fun onChanged() {
                requestLayout()
            }
        }

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    fun bindViewPager(pager: ViewPager) {
        try {
            this.pager.dupicateAssign(pager) ?: return
            this.pager.adapter.unregisterDataSetObserver(observer)
        } catch (e: UninitializedPropertyAccessException) {
            // do nothing
        }

        checkNotNull(pager, "ViewPager")

        pager.addOnPageChangeListener(listener)
        position = pager.currentItem
        pager.adapter.registerDataSetObserver(observer)
        this.pager = pager
        requestLayout()
    }

    fun setRadius(radius: Float) {
        this.radius.dupicateAssign(radius) ?: return
        this.radius = radius
        requestLayout()
    }

    fun setDividerWidth(dividerWidth: Float) {
        this.dividerWidth.dupicateAssign(dividerWidth) ?: return
        this.dividerWidth = dividerWidth
        requestLayout()
    }

    fun setNormalColor(@ColorInt color: Int) {
        normalColor.dupicateAssign(color) ?: return
        normalColor = color
        invalidate()
    }

    fun setSelectedColor(@ColorInt color: Int) {
        selectedColor.dupicateAssign(color) ?: return
        selectedColor = color
        invalidate()
    }

    fun showInSinglePage(showInSinglePage: Boolean) {
        if (this.showInSinglePage == showInSinglePage) {
            return
        }

        this.showInSinglePage = showInSinglePage
        try {
            if (pager.adapter.count == 1) {
                requestLayout()
            }
        } catch (e: UninitializedPropertyAccessException) {
            // do nothing
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = pager.adapter.count.check()
        if (count == null) {
            setMeasuredDimension(0, 0)
            return
        }

        if (orientation == Orientation.HORIZONTAL) {
            measureHorizontal(count, widthMeasureSpec, heightMeasureSpec)
        } else {
            measureVertical(count, heightMeasureSpec, widthMeasureSpec)
        }
    }

    private fun measureHorizontal(count: Int, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val trulyWidth = count * radius * 2 + (count - 1) * dividerWidth
        val width = when (widthMode) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST ->
                trulyWidth.toInt()

            MeasureSpec.EXACTLY ->
                MeasureSpec.getSize(widthMeasureSpec)

            else -> 0
        }

        val height = when (heightMode) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST ->
                (radius * 4).toInt()

            MeasureSpec.EXACTLY ->
                MeasureSpec.getSize(heightMeasureSpec)

            else -> 0
        }

        centerY = height / 2F
        startX = (width - trulyWidth) / 2F
        if (startX < 0) {
            startX = 0F
        }

        setMeasuredDimension(width, height)
    }

    private fun measureVertical(count: Int, heightMeasureSpec: Int, widthMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val trulyHeight = count * radius * 2 + (count - 1) * dividerWidth
        val height = when (heightMode) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST ->
                trulyHeight.toInt()

            MeasureSpec.EXACTLY ->
                MeasureSpec.getSize(heightMeasureSpec)

            else -> 0
        }

        val width = when (widthMode) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST ->
                (radius * 4).toInt()

            MeasureSpec.EXACTLY ->
                MeasureSpec.getSize(widthMeasureSpec)

            else -> 0
        }

        centerX = width / 2F
        startY = (height - trulyHeight) / 2F
        if (startY < 0) {
            startY = 0F
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        val count = pager.adapter.count.check() ?: return
        if (orientation == Orientation.HORIZONTAL) {
            drawHorizontal(count, canvas)
        } else {
            drawVertical(count, canvas)
        }
    }

    private fun drawHorizontal(count: Int, canvas: Canvas?) {
        for (i in 0..(count - 1)) {
            val radiusX = startX + radius * (i * 2 + 1) + dividerWidth * i
            drawIndicator(radiusX, centerY, canvas, i)
        }

        if (positionOffset > 0) {
            val pStartX = startX + radius * (position * 2 + 1) + dividerWidth * position
            val pEndX = pStartX + positionOffset * (dividerWidth + radius * 2)
            paint.color = selectedColor
            canvas?.drawCircle(pEndX, centerY, radius, paint)
        }
    }

    private fun drawVertical(count: Int, canvas: Canvas?) {
        for (i in 0..(count - 1)) {
            val radiusY = startY + radius * (i * 2 + 1) + dividerWidth * i
            drawIndicator(centerX, radiusY, canvas, i)
        }

        if (positionOffset > 0) {
            val pStartY = startY + radius * (position * 2 + 1) + dividerWidth * position
            val pEndY = pStartY + positionOffset * (dividerWidth + radius * 2)
            paint.color = selectedColor
            canvas?.drawCircle(centerX, pEndY, radius, paint)
        }
    }

    fun drawIndicator(centerX: Float, centerY: Float, canvas: Canvas?, position: Int) {
        if (this.position == position && positionOffset == 0F) {
            paint.color = selectedColor
        } else {
            paint.color = normalColor
        }
        canvas?.drawCircle(centerX, centerY, radius, paint)
    }

    private fun Int.check(): Int? {
        val pageLimit = when (showInSinglePage) {
            true -> 0
            false -> 1
        }
        if (this <= pageLimit) {
            return null
        }

        return this
    }

    private fun Any.dupicateAssign(any: Any): Any? {
        if (this == any) {
            return null
        }

        return this
    }
}