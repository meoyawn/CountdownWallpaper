package adeln.countdown

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import org.jetbrains.anko.ctx

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

class SettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceScreen = preferenceManager.createPreferenceScreen(ctx)

        val p = Preference(ctx).apply {

        }

        preferenceScreen.addPreference(p)
    }
}

@SuppressLint("ExportedPreferenceActivity")
class SettingsActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .add(android.R.id.content, SettingsFragment())
                .commit()
            fragmentManager.executePendingTransactions()
        }
    }
}
