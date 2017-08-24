package adeln.countdown

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.service.wallpaper.WallpaperService
import android.view.Choreographer
import android.view.SurfaceHolder

class CountdownService : WallpaperService() {

    // fucking android
    inner class CountdownEngine : Engine() {

        val drawer = Choreographer.FrameCallback { draw() }

        val textP = Paint().also {
            it.color = Color.WHITE
            it.textSize = 50F
        }

        override fun onVisibilityChanged(visible: Boolean) =
            if (visible) draw() else Unit

        fun draw() {
            if (!isVisible) {
                return
            }

            surfaceHolder.canvas {
                it.drawColor(Color.BLACK)
                it.drawText("ТЫ ПИДР", it.width / 2F, it.height / 2F, textP)
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
