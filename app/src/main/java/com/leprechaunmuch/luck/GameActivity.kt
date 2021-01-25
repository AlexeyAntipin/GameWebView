package com.leprechaunmuch.luck

import android.graphics.drawable.Drawable
import android.os.*
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.leprechaunmuch.luck.Game.Coin
import com.leprechaunmuch.luck.Game.GameState
import com.leprechaunmuch.luck.Game.GameView
import java.util.*
import kotlin.math.max
import kotlin.math.min


class GameActivity : AppCompatActivity() {

    val random : Random = Random()

    lateinit var gameView: GameView
    lateinit var startButton: Button
    lateinit var layout: LinearLayout

    var coins = mutableListOf<Coin>()

    lateinit var timer: CountDownTimer
    var score: Int = 0

    var gameState : GameState = GameState.Pause
    lateinit var _coins: MutableList<Drawable>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_game)

        _coins = mutableListOf(getDrawable(R.drawable.coin1)!!,
            getDrawable(R.drawable.coin2)!!,
            getDrawable(R.drawable.coin3)!!)

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

    fun gameTap(x: Float, y: Float){
        if (gameState == GameState.Pause) {
            return
        }

        if (gameState == GameState.InProcess) {
            synchronized(this) {
                for (coin in coins) {
                    if (Utils.Math.dist(x, y, coin.GetX(), coin.GetY()) < Coin.size) {
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
                var coin: Coin = Coin(Utils.Math.rand(random, 2 * Coin.size, gameView.width - 2 * Coin.size) * 1f,
                    50f, _coins[Random().nextInt(3)])
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
                    speed = 4
                else if (coin.ttl in 50..100)
                    speed = 3
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