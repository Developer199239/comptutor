package com.example.comptutor.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SessionHelper(val context: Context) {
    companion object {
        private const val PREF_NAME = "com.example.comptutor"
        public const val USER_ID = "userId"
        public const val FIREBASE_TOKEN = "firebaseToken"
        public const val UPLOAD_FIREBASE_TOKEN_SUCCESS = "uploadFirebaseToken"
        public const val LOGIN_INFO = "loginInfo"
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

    fun setLoginInfo(loginInfo: StudentModel){
        val s = Gson().toJson(loginInfo)
        editor!!.putString(LOGIN_INFO, s)
        editor!!.commit()
    }

    fun getLoginInfo(): StudentModel{
        val s = preferences.getString(LOGIN_INFO, "")
        return try {
            Gson().fromJson(s, StudentModel::class.java)
        } catch (e: Exception) {
            StudentModel()
        }
    }

    fun clearSession() {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor!!.clear()
        editor!!.apply()
    }
}