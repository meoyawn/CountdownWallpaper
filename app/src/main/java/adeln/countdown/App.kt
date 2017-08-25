package adeln.countdown

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid
import org.jetbrains.anko.ctx

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(ctx)
    }
}
