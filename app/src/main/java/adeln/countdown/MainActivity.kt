package adeln.countdown

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceActivity
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
    }
}

class SettingsActivity : PreferenceActivity() {

}
