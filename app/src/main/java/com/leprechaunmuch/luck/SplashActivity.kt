package com.leprechaunmuch.luck

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.github.kittinunf.fuel.httpGet

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        
        var spr = SharedPreferencesRegistry(
            getSharedPreferences(SharedPreferencesRegistry.spName, Context.MODE_PRIVATE))

        SharedPreferencesRegistry.url.httpGet().responseString {
                request, response, result ->
            if (response.responseMessage == "OK") {
                spr.put(SharedPreferencesRegistry.savedUrl, result.get())
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