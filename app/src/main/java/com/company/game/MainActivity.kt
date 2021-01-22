package com.company.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DrawFilter
import android.graphics.Picture
import android.graphics.drawable.Drawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.company.game.Game.Coin
import com.company.game.Game.GameState
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    //val registry: IRegistry = TODO()

    val gameView: GameView = GameView(this)
    val startButton: Button = findViewById(R.id.startButton)

    var gameState: GameState = GameState.WaitingFirstLaunch
    var coins: List<Coin> = ArrayList<Coin>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener(
            View.OnClickListener {
                startGame();
            }
        )
    }

    fun startGame(){
        startButton.visibility = View.INVISIBLE
        gameView.visibility = View.VISIBLE

        gameState = GameState.InProgress

        coins = ArrayList<Coin>()

        val gameLoop : TimerTask = GameLoopTask()
        val gameTimer : Timer = Timer()

        gameTimer.schedule(gameLoop, 25)
    }

    inner class GameLoopTask : TimerTask(){
        override fun run() { this@MainActivity.gameView.reDraw() }
    }

    inner class GameView(context: Context?) : View(context){

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            var background = this@MainActivity.getDrawable(R.drawable.game_background)
            if (canvas != null) {
                background?.setBounds(0, 0, canvas.width, canvas.height)
                background?.draw(canvas)
            }
        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            return super.onTouchEvent(event)
        }

        fun reDraw() {

        }
    }

}