package com.example.comptutor.utils

import android.content.Context
import android.content.SharedPreferences

class SessionHelper(val context: Context) {
    companion object {
        private const val PREF_NAME = "com.example.comptutor"
        public const val USER_ID = "userId"
    }

    private var preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor? = null

    init {
        editor = preferences.edit()
    }

    fun setStringValue(key: String, value: String){
        editor!!.putString(key, value)
        editor!!.commit()
    }

    fun getStringValue(key: String): String {
        return preferences.getString(key, "")!!
    }
}