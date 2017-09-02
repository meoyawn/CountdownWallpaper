package adeln.countdown

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
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
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.MutableInterval
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

val TARGET: DateTime =
    LocalDate(1992, DateTimeConstants.JULY, 6)
        .plusYears(67)
        .toDateTime(LocalTime(0, 0))

val DELAY: Long =
    TimeUnit.SECONDS.toMillis(1)

val INTERVAL = MutableInterval(System.currentTimeMillis(), TARGET.millis)

val THIN: Typeface =
    Typeface.create("sans-serif-thin", Typeface.NORMAL)

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

        years = addColumn(column, R.string.years)
        months = addColumn(column, R.string.months)
        days = addColumn(column, R.string.days)
        hours = addColumn(column, R.string.hours)
        minutes = addColumn(column, R.string.minutes)
        seconds = addColumn(column, R.string.seconds)
    }

    return ViewHolder(view = v,
                      years = years,
                      months = months,
                      days = days,
                      hours = hours,
                      minutes = minutes,
                      seconds = seconds)
}

typealias StringRes = Int

fun @AnkoViewDslMarker _GridLayout.addColumn(column: MutableInt, txt: StringRes): TextView {

    val tv = textView {
        text = "top"
        textColor = Color.WHITE
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        textSize = 28F
        typeface = THIN
    }.lparams {
        rowSpec = GridLayout.spec(0, 1F)
        columnSpec = GridLayout.spec(column.value, 1F)
    }

    textView {
        textResource = txt
        textColor = Color.WHITE
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        typeface = THIN
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

            val desiredWidth = resources.displayMetrics.widthPixels
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

            Choreographer.getInstance().postFrameCallbackDelayed(drawer, DELAY)
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
