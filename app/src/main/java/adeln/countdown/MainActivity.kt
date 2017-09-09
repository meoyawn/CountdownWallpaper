package adeln.countdown

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.DialogPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.View
import android.widget.DatePicker
import org.jetbrains.anko.ctx
import org.joda.time.LocalDate

inline fun <reified T> Context.componentName(): ComponentName =
    ComponentName(ctx, T::class.java)

fun Context.changeLiveWallpaper(): Intent =
    Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName<CountdownService>())

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(changeLiveWallpaper())
        finish()
    }
}

class DatePreference(ctx: Context) : DialogPreference(ctx) {

    lateinit var dp: DatePicker

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)
        summary = getPersistedString(null)
    }

    override fun onCreateDialogView(): View =
        DatePicker(context).also { dp = it }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        val dt = getPersistedString(null)
            ?.let { LocalDate.parse(it) }
            ?: LocalDate.now()

        dp.updateDate(dt.year, dt.monthOfYear - 1, dt.dayOfMonth)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)
        if (!positiveResult) return

        val str = LocalDate(dp.year, dp.month + 1, dp.dayOfMonth).toString()
        persistString(str)
        summary = str
    }
}

@SuppressLint("ExportedPreferenceActivity")
class SettingsActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val date = DatePreference(ctx).also {
            it.key = "date"
            it.setTitle(R.string.date)
        }

        preferenceScreen = preferenceManager.createPreferenceScreen(ctx).also { it.addPreference(date) }
    }
}
