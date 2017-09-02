package adeln.countdown

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.service.wallpaper.WallpaperService
import android.util.MutableInt
import android.view.Choreographer
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import org.jetbrains.anko.AnkoViewDslMarker
import org.jetbrains.anko._GridLayout
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.gridLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.MutableInterval
import kotlin.properties.Delegates

val TARGET: DateTime =
    LocalDate(1992, DateTimeConstants.JULY, 6)
        .plusYears(67)
        .toDateTime(LocalTime(0, 0))

val INTERVAL = MutableInterval(System.currentTimeMillis(), TARGET.millis)

class ViewHolder(
    val view: View,
    val years: TextView,
    val months: TextView,
    val days: TextView,
    val hours: TextView,
    val minutes: TextView,
    val seconds: TextView
)

fun Context.mkViewHolder(): ViewHolder {
    var years: TextView by Delegates.notNull()
    var months: TextView by Delegates.notNull()
    var days: TextView by Delegates.notNull()
    var hours: TextView by Delegates.notNull()
    var minutes: TextView by Delegates.notNull()
    var seconds: TextView by Delegates.notNull()

    val column = MutableInt(0)

    val v = gridLayout {
        layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)

        rowCount = 2
        columnCount = 6
        backgroundColor = Color.BLACK

        years = addColumn(column)
        months = addColumn(column)
        days = addColumn(column)
        hours = addColumn(column)
        minutes = addColumn(column)
        seconds = addColumn(column)
    }

    return ViewHolder(view = v,
                      years = years,
                      months = months,
                      days = days,
                      hours = hours,
                      minutes = minutes,
                      seconds = seconds)
}

fun @AnkoViewDslMarker _GridLayout.addColumn(column: MutableInt): TextView {

    val tv = textView {
        text = "top"
        textColor = Color.WHITE
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
    }.lparams {
        rowSpec = GridLayout.spec(0, 1F)
        columnSpec = GridLayout.spec(column.value, 1F)
    }

    textView {
        text = "bottom"
        textColor = Color.WHITE
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
    }.lparams {
        rowSpec = GridLayout.spec(1, 1F)
        columnSpec = GridLayout.spec(column.value, 1F)
    }

    column.value++

    return tv
}

class CountdownService : WallpaperService() {

    // fucking android
    inner class CountdownEngine : Engine() {

        val drawer = Choreographer.FrameCallback { draw() }
        val vh = mkViewHolder()

        override fun onVisibilityChanged(visible: Boolean) {
            if (!visible) return

            val desiredWidth = desiredMinimumWidth
            val desiredHeight = desiredMinimumHeight
            val w = View.MeasureSpec.makeMeasureSpec(desiredWidth, View.MeasureSpec.AT_MOST)
            val h = View.MeasureSpec.makeMeasureSpec(desiredHeight, View.MeasureSpec.AT_MOST)

            vh.view.measure(w, h)
            vh.view.layout(0, 0, desiredWidth, desiredHeight)

            draw()
        }

        fun draw() {
            if (!isVisible) {
                return
            }

            surfaceHolder.canvas {
                vh.view.draw(it)
            }

            Choreographer.getInstance().postFrameCallback(drawer)
        }
    }

    override fun onCreateEngine(): Engine =
        CountdownEngine()
}

inline fun <T> SurfaceHolder.canvas(f: (Canvas) -> T): T =
    lockCanvas().let {
        try {
            f(it)
        } finally {
            unlockCanvasAndPost(it)
        }
    }
