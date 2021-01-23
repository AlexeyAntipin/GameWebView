package com.company.game

import android.content.SharedPreferences
import java.lang.Exception

class SharedPreferencesRegistry : IRegistry{

    private val sp: SharedPreferences

    constructor(sp: SharedPreferences) {
        this.sp = sp
    }

    companion object {
        val url = "https://google.com"
        val cookieName = "user"
        val spName = "save"
        val saveLink = "link"
        val getUrl = "url"
    }
    override fun get(key: String): String {
        if (has(key) && sp.contains(key))
            return sp.getString(key, "").toString()
        else return ""
    }

    override fun has(key: String): Boolean {
        if (sp.all.size != 0 && sp.contains(key))
            return true
        return false
    }

    override fun put(key: String, value: String): Boolean {
        try {
            var editor = sp.edit()
            editor.putString(key, value)
            editor.apply()
            return true
        }catch (e: Exception) {
            return false
        }
    }
}