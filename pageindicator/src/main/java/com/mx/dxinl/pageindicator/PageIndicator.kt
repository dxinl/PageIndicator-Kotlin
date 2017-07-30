package com.mx.dxinl.pageindicator

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Paint
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

    private var pager: ViewPager? = null
    private val listener: ViewPager.OnPageChangeListener
    private val observer: DataSetObserver
    private val paint: Paint

    private val radius: Float
    private val dividerWidth: Float
    private val selectedColor: Int
    private val normalColor: Int
    private var centerY: Float = 0F
    private var startX: Float = 0F
    private var position: Int = 0
    private var positionOffset: Float = 0F

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
        this.pager?.adapter?.unregisterDataSetObserver(observer)

        checkNotNull(pager, "ViewPager")

        if (pager == this.pager) {
            return
        }

        pager.addOnPageChangeListener(listener)
        position = pager.currentItem
        pager.adapter.registerDataSetObserver(observer)
        this.pager = pager
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = pager?.adapter?.count?.check()
        if (count == null) {
            setMeasuredDimension(0, 0)
            return
        }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val trulyWidth = count * radius * 2 + (count - 1) * dividerWidth
        val width = when (widthMode) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST ->
                trulyWidth.toInt()

            MeasureSpec.EXACTLY ->
                MeasureSpec.getSize(widthMeasureSpec)

            else -> 0
        }

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
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

    override fun onDraw(canvas: Canvas?) {
        val count = pager?.adapter?.count?.check() ?: return
        for (i in 0..(count - 1)) {
            val radiusX = startX + radius * (i * 2 + 1) + dividerWidth * i
            if (i == position && positionOffset == 0F) {
                paint.color = selectedColor
            } else {
                paint.color = normalColor
            }
            canvas?.drawCircle(radiusX, centerY, radius, paint)
        }

        if (positionOffset > 0) {
            val pStartX = startX + radius * (position * 2 + 1) + dividerWidth * position
            val pEndX = pStartX + positionOffset * (dividerWidth + radius * 2)
            paint.color = selectedColor
            canvas?.drawCircle(pEndX, centerY, radius, paint)
        }
    }

    private fun Int.check(): Int? {
        if (this <= 0) {
            return null
        }

        return this
    }
}