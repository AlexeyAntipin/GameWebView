package com.company.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import com.company.game.Game.Coin
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

    override fun onCreate(savedInstanceState: Bundle?) {
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
        score = 0;

        startButton.visibility = View.INVISIBLE

        coins = mutableListOf()

        gameView = GameView(this)
        gameView.gameActivity = this

        layout.addView(gameView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT))

        timer = object: CountDownTimer(1000000000, 60) {
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
        synchronized(this) {
            for(coin in coins){
                if (dist(x, y, coin.GetX(), coin.GetY()) < Coin.size){
                    coin.tapped = true
                    score++
                    break
                }
            }
        }
    }

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

                coin.Move(0f, 8f)

                if (coin.GetY() < gameView.height - Coin.size * 2) {
                    temp_coins.add(coin)
                } else {
                    timer.cancel()

                    startButton.text = "Начать заново"
                    startButton.visibility = View.VISIBLE

                    gameView.stop()

                    layout.removeViewInLayout(gameView)
                    break
                }
            }

            coins = temp_coins
        }
    }
}