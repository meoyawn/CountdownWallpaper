package adeln.countdown

import android.service.wallpaper.WallpaperService

class CountdownService : WallpaperService() {
    override fun onCreateEngine(): Engine =
        CountdownEngine()
}
