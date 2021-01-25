package com.leprechaunmuch.luck

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
import android.view.Window
import android.view.WindowManager
import android.webkit.*
import com.onesignal.OneSignal
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig

class MainActivity : AppCompatActivity() {

    private val devKey = "qrdZGj123456789"
    val ONESIGNAL_APP_ID = "83cbd7df-ffad-48a0-be36-29839beeea36"
    val APPMETRICA_API_KEY = "43f71d9e-837c-4213-9e88-e1edd6dfb963"
    var link = ""
    lateinit var spr: SharedPreferencesRegistry
    var flag = false

    private val FILECHOOSER_RESULTCODE = 1
    var uploadMessage: ValueCallback<Array<Uri>>? = null

    lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {

        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        super.onCreate(savedInstanceState)

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        val config = YandexMetricaConfig.newConfigBuilder(APPMETRICA_API_KEY).build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(application)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        spr = SharedPreferencesRegistry(
            getSharedPreferences(SharedPreferencesRegistry.spName, Context.MODE_PRIVATE))
        if (spr.has(SharedPreferencesRegistry.savedLink)) {
            link = spr.get(SharedPreferencesRegistry.savedLink)
        } else if (spr.has(SharedPreferencesRegistry.savedUrl)) {
            link = spr.get(SharedPreferencesRegistry.savedUrl)
        }
        web = findViewById(R.id.web)

        if (link == "") {
            startActivity(Intent(this@MainActivity, GameActivity::class.java))
        }
        else {
            setWebView()
        }
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
        var cookie = getCookie(link, SharedPreferencesRegistry.cookieName)

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
                spr.put(SharedPreferencesRegistry.savedLink, url.toString())
                link = url.toString()
                clearCookies(link, url.toString())
                cookieManager.setCookie(link, "$SharedPreferencesRegistry.cookieName=$url")
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
}