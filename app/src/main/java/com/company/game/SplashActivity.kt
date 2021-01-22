package com.company.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.github.kittinunf.fuel.httpGet

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var spr = SharedPreferencesRegistry(getSharedPreferences("save", Context.MODE_PRIVATE))

        val vackground = resources.getDrawable(R.drawable.item_for_splash)

        SharedPreferencesRegistry.url.httpGet().responseString {
                request, response, result ->
            if (response.responseMessage == "OK") {
                spr.put("url", result.get())
            } else {
                Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_LONG).show()
            }
        }

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}