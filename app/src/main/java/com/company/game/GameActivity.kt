package com.company.game

import android.content.Context
import android.os.*
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.company.game.Game.Coin
import com.company.game.Game.GameState
import com.company.game.Game.GameView
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class GameActivity : AppCompatActivity() {

    val random : Random = Random()

    lateinit var gameView: GameView
    lateinit var startButton: Button
    lateinit var layout: LinearLayout

    var coins = mutableListOf<Coin>()

    lateinit var timer: CountDownTimer
    var score: Int = 0

    var gameState : GameState = GameState.Pause

    override fun onCreate(savedInstanceState: Bundle?) {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_game)

        layout = findViewById(R.id.mainLayout)

        startButton = findViewById(R.id.startButton)
        startButton.visibility = View.VISIBLE
        startButton.setOnClickListener(View.OnClickListener { startGame() })
    }

    fun startGame(){
        score = 0
        gameState = GameState.InProcess

        startButton.visibility = View.INVISIBLE

        coins = mutableListOf()

        gameView = GameView(this)
        gameView.gameActivity = this

        layout.addView(gameView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT))

        timer = object: CountDownTimer(1000000000, 20) {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTick(millisUntilFinished: Long) { gameTick() }
            override fun onFinish() {}
        }

        timer.start()
    }

    fun rand(from : Int, to : Int) : Int{
        return random.nextInt(to - from) + from
    }

    fun dist(x1: Float, y1 : Float, x2 : Float, y2 : Float) : Float {
        return sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble()).toFloat()
    }

    fun gameTap(x: Float, y: Float){
        if (gameState == GameState.Pause) {
            return
        }

        if (gameState == GameState.InProcess) {
            synchronized(this) {
                for (coin in coins) {
                    if (dist(x, y, coin.GetX(), coin.GetY()) < Coin.size) {
                        coin.tapped = true
                        score++
                        break
                    }
                }
            }
        }

        if (gameState == GameState.End){
            startGame()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun gameTick(){
        synchronized(this) {

            if (random.nextInt(100) <= min(25, max( 5, score / 5))) {
                var coin: Coin = Coin(rand(2 * Coin.size, gameView.width - 2 * Coin.size) * 1f, 50f)
                coins.add(coin)
            }

            var temp_coins = mutableListOf<Coin>()

            for (coin in coins) {
                if (coin.tapped) {
                    continue
                }

                coin.ttl++

                var speed : Int = 8

                if (coin.ttl < 50)
                    speed = 8
                else if (coin.ttl in 50..100)
                    speed = 6
                else speed = 2


                coin.Move(0f, speed * 1f)

                if (coin.GetY() < gameView.height - Coin.size * 2) {
                    temp_coins.add(coin)
                } else {
                    timer.cancel()

                    gameState = GameState.End
                    temp_coins = coins
                    break
                }
            }

            coins = temp_coins
        }
    }
}