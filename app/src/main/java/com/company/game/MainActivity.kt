package com.company.game

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*

class MainActivity : AppCompatActivity() {

    val ONESIGNAL_APP_ID = ""
    val APPMETRICA_API_KEY = ""
    val cookieName = "user"
    val spName = "save"
    val saveLink = "link"
    val getUrl = "url"
    var link = ""
    lateinit var spr: SharedPreferencesRegistry
    var flag = false

    private val FILECHOOSER_RESULTCODE = 1
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath = ""

    lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spr = SharedPreferencesRegistry(getSharedPreferences(spName, Context.MODE_PRIVATE))
        if (spr.has(saveLink)) {
            link = spr.get(saveLink)
        } else if (spr.has(getUrl)) {
            link = spr.get(getUrl)
        }

        web = findViewById(R.id.web)
        setWebView()
    }


    private fun getCookie(url: String, name: String): String {
        var value = ""
        var cookie_manager = CookieManager.getInstance()
        var cookies = cookie_manager.getCookie(url)
        if (cookies != null) {
            var temp = cookies.split(";")
            for (str in temp) {
                if (str.contains(name)) {
                    var temp1 = str.split("=")
                    value = temp1[1]
                    for (i in 2..(temp1.size - 1)) value += '=' + temp1[i]
                }
            }
        }
        return value
    }

    fun clearCookies(domain: String?, url: String) {
        CookieSyncManager.createInstance(this)
        val cookieManager = CookieManager.getInstance()
        val cookiestring = cookieManager.getCookie(domain)
        if (cookiestring != null) {
            val cookies = cookiestring.split(";".toRegex()).toTypedArray()
            for (i in cookies.indices) {
                val cookieparts = cookies[i].split("=".toRegex()).toTypedArray()
                cookieManager.setCookie(domain, cookieparts[0].trim { it <= ' ' } + "=$url")
            }
        }
        CookieSyncManager.getInstance().sync()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != FILECHOOSER_RESULTCODE || uploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            var results: Array<Uri>? = null

            if (resultCode === Activity.RESULT_OK) {
                if (data != null) {
                    val dataString = data.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
            uploadMessage!!.onReceiveValue(results)
            uploadMessage = null
        }

        return
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            if (event.keyCode == KeyEvent.KEYCODE_BACK || web.canGoBack()) {
                web.goBack()
            } else if (event.keyCode != KeyEvent.KEYCODE_BACK) {
                super.onKeyDown(keyCode, event)
            }
        }
        return true
    }

    private fun setWebView() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(web, true)
        var cookie = getCookie(link, cookieName)

        web.webViewClient = object : WebViewClient() {
            override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
                if (event != null) {
                    if (event.keyCode == KeyEvent.KEYCODE_BACK || web.canGoBack()) {
                        web.goBack()
                    } else if (event.keyCode != KeyEvent.KEYCODE_BACK) {
                        super.onUnhandledKeyEvent(view, event)
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (!flag) {
                    web.visibility = View.VISIBLE
                    flag = true
                }
                Log.d("MyLog", url.toString())
                spr.put(saveLink, url.toString())
                link = url.toString()
                clearCookies(link, url.toString())
                cookieManager.setCookie(link, "$cookieName=$url")
            }


        }

        web.webChromeClient = object : WebChromeClient() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(mWebView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }

                uploadMessage = filePathCallback

                var contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.setType("image/*")

                var intentArray: Array<Intent?>

                intentArray = arrayOfNulls(0)


                var chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)

                return true
            }
        }

        web.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        web.settings.javaScriptEnabled = true
        web.settings.loadsImagesAutomatically = true
        web.settings.domStorageEnabled = true
        web.settings.loadWithOverviewMode = true
        web.settings.useWideViewPort = true
        web.settings.builtInZoomControls = true
        web.settings.displayZoomControls = false
        web.settings.databaseEnabled = true
        web.settings.allowContentAccess = true
        web.settings.allowFileAccess = true
        web.settings.javaScriptCanOpenWindowsAutomatically = true
        web.settings.setSupportZoom(true)
        web.settings.defaultTextEncodingName = "utf-8"
        web.settings.pluginState = WebSettings.PluginState.ON

        if (cookie != "") web.loadUrl(cookie)
        else web.loadUrl(link)
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