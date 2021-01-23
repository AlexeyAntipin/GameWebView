package com.company.game.Game

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.company.game.GameActivity
import com.company.game.R


class GameView(context: Context?) : SurfaceView(context), SurfaceHolder.Callback {

    var gameActivity : GameActivity? = null;
    lateinit var drawThread : DrawThread;

    init{
        setZOrderOnTop(true)
        var holder = getHolder()
        holder.addCallback (this)
    }


    constructor(context: Context?, attributes: AttributeSet) : this(context) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawThread = DrawThread(getHolder(), resources)
        drawThread.setRunning(true)
        drawThread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        drawThread.setRunning(false)
        while (retry) {
            try {
                drawThread.join()
                retry = false
            } catch (e: InterruptedException) {

            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gameActivity?.gameTap(event.x * 1f, event.y * 1f)
        }
        return super.onTouchEvent(event)
    }

    fun stop(){
        drawThread.setRunning(false)
    }


    inner class DrawThread(private val surfaceHolder: SurfaceHolder, resources: Resources?) : Thread() {
        private var runFlag = false
        private lateinit var _coin: Drawable;
        private lateinit var _background: Drawable;

        fun setRunning(run: Boolean) {
            runFlag = run
        }

        override fun run() {
            var canvas: Canvas?
            _coin = gameActivity?.getDrawable(R.drawable.coin)!!
            _background = gameActivity?.getDrawable(R.drawable.background)!!
            _background.setBounds(0, 0, width, height)

            while (runFlag) {
                canvas = null
                try {
                    canvas = surfaceHolder.lockCanvas(null)
                    synchronized(surfaceHolder) {
                        _background.draw(canvas)

                        synchronized(gameActivity!!){
                            for (coin in gameActivity?.coins!!) {
                                if (coin.tapped) {
                                    continue
                                }

                                _coin.setBounds(
                                    (coin.GetX() - Coin.size).toInt(), (coin.GetY() - Coin.size).toInt(),
                                    (coin.GetX() + Coin.size).toInt(), (coin.GetY() + Coin.size).toInt())
                                _coin.draw(canvas)
                            }
                        }

                        var paint = Paint()
                        paint.color = Color.WHITE
                        paint.textSize = 80f
                        paint.style = Paint.Style.FILL_AND_STROKE

                        var w = paint.measureText(gameActivity?.score.toString())
                        canvas.drawText(gameActivity?.score.toString(), (width - w) / 2, 100f, paint)
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
            }
        }

    }
}