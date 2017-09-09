package adeln.countdown

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.preference.PreferenceManager
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
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.gridLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.MutableInterval
import org.joda.time.Period
import org.joda.time.PeriodType
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

val DELAY: Long =
    TimeUnit.SECONDS.toMillis(1)

val INTERVAL: MutableInterval =
    MutableInterval(System.currentTimeMillis(), System.currentTimeMillis())

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

fun ViewHolder.bind(p: Period) {
    years.text = p.years.toString()
    months.text = p.months.toString()
    days.text = p.days.toString()
    hours.text = p.hours.toString()
    minutes.text = p.minutes.toString()
    seconds.text = p.seconds.toString()
}

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
        padding = dip(16)
        topPadding = dip(100)

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
        textColor = Color.WHITE
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        textSize = 38F
        typeface = THIN
    }.lparams {
        rowSpec = GridLayout.spec(0, 1F)
        columnSpec = GridLayout.spec(column.value, 1F)
    }

    textView {
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        textResource = txt
        textColor = Color.LTGRAY
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

            INTERVAL.endMillis = PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString("date", null)
                ?.let { LocalDate.parse(it) }
                ?.toDateTime(LocalTime(0, 0))
                ?.millis
                ?.takeIf { it > INTERVAL.startMillis }
                ?: DateTime.now().plusDays(1).millis

            val desiredWidth = resources.displayMetrics.widthPixels
            val desiredHeight = desiredMinimumHeight
            val w = View.MeasureSpec.makeMeasureSpec(desiredWidth, View.MeasureSpec.AT_MOST)
            val h = View.MeasureSpec.makeMeasureSpec(desiredHeight, View.MeasureSpec.AT_MOST)

            vh.view.measure(w, h)
            vh.view.layout(0, 0, desiredWidth, desiredHeight)

            draw()
        }

        fun draw() {
            if (!isVisible) return

            INTERVAL.startMillis = System.currentTimeMillis()
            vh.bind(INTERVAL.toPeriod(PeriodType.yearMonthDayTime()))

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
