package com.leprechaunmuch.luck.Game

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
import com.leprechaunmuch.luck.GameActivity
import com.leprechaunmuch.luck.R
import com.leprechaunmuch.luck.Utils


class GameView(context: Context?) : SurfaceView(context), SurfaceHolder.Callback {

    var gameActivity : GameActivity? = null
    lateinit var drawThread : DrawThread

    init{
        setZOrderOnTop(true)
        var holder = getHolder()
        holder.addCallback (this)
    }

    constructor(context: Context?, attributes: AttributeSet) : this(context) {}

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
        private lateinit var _coins: MutableList<Drawable>
        private lateinit var _background: Drawable

        fun setRunning(run: Boolean) {
            runFlag = run
        }

        override fun run() {
            var canvas: Canvas?
            _coins = mutableListOf(gameActivity?.getDrawable(R.drawable.coin1)!!,
                gameActivity?.getDrawable(R.drawable.coin2)!!,
                gameActivity?.getDrawable(R.drawable.coin3)!!)
            _background = gameActivity?.getDrawable(R.drawable.background)!!
            _background.setBounds(0, 0, width, height)

            while (runFlag) {
                canvas = null
                try {
                    canvas = surfaceHolder.lockCanvas(null)
                    synchronized(surfaceHolder) {
                        _background.draw(canvas)

                        synchronized(gameActivity!!){
                            Coin.size = (height * 0.07).toInt()

                            for (coin in gameActivity?.coins!!) {
                                if (coin.tapped) {
                                    continue
                                }

                                var r = java.util.Random().nextInt(2)
                                coin.drawable.setBounds(
                                    (coin.GetX() - Coin.size).toInt(), (coin.GetY() - Coin.size).toInt(),
                                    (coin.GetX() + Coin.size).toInt(), (coin.GetY() + Coin.size).toInt())
                                coin.drawable.draw(canvas)
                            }
                        }

                        var paint = Paint()
                        paint.color = Color.WHITE
                        paint.textSize = Utils.Surface.getTextSizeByWidth(
                                paint, gameActivity?.score.toString(), (width * 0.15).toInt())
                        paint.style = Paint.Style.FILL_AND_STROKE

                        var w = paint.measureText(gameActivity?.score.toString())
                        canvas.drawText(gameActivity?.score.toString(),
                                (width - w) / 2, 10f + paint.textSize, paint)

                        if (gameActivity?.gameState == GameState.End){
                            val endGameText ="Нажмите в любом месте, чтобы начать заново"

                            val paint2 = Paint()
                            paint2.color = Color.WHITE
                            paint2.textSize = Utils.Surface.getTextSizeByWidth(
                                    paint2, endGameText, (width * 0.7).toInt())
                            paint2.style = Paint.Style.FILL_AND_STROKE

                            w = paint2.measureText(endGameText)
                            canvas.drawText(endGameText,
                                    (width - w) / 2, height - 60f, paint2)
                        }
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