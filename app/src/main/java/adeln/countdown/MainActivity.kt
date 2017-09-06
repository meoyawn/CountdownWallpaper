package adeln.countdown

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
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

class DatePickerFragment : DialogFragment() {

    companion object {
        fun new(ld: LocalDate): DatePickerFragment =
            DatePickerFragment().also {
                it.arguments = Bundle().also {
                    it.putSerializable("date", ld)
                }
            }
    }

    fun dateArg(): LocalDate =
        arguments.getSerializable("date") as LocalDate

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
            LocalDate(y, m, d)
        }

        val dt = dateArg()
        return DatePickerDialog(ctx, listener, dt.year, dt.monthOfYear, dt.dayOfMonth)
    }
}

fun FragmentTransaction.commitNow(mgr: FragmentManager) {
    commit()
    mgr.executePendingTransactions()
}

@SuppressLint("ExportedPreferenceActivity")
class SettingsActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceScreen = preferenceManager.createPreferenceScreen(ctx)
        val p = Preference(ctx).apply {
            setOnPreferenceClickListener {
                fragmentManager.beginTransaction()
                    .add(DatePickerFragment.new(LocalDate.now()), "fuck")
                    .commitNow(fragmentManager)
                true
            }
        }
        preferenceScreen.addPreference(p)
    }
}
