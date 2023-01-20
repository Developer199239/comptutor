package com.example.comptutor.utils

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.comptutor.Register
import com.google.firebase.firestore.FirebaseFirestore
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.enums.PNPushType
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.push.PNPushAddChannelResult
import java.util.*


class ComptutorApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        instance = this
        initPubnub()
    }

    companion object {
        private lateinit var instance: ComptutorApplication
        var classModel: ClassModel = ClassModel()
        var studentsList: ArrayList<StudentModel> = ArrayList()
        var pubnub: PubNub? = null

        public fun getContext(): Context {
            return instance
        }
        fun sendRegistrationToPubNub(token: String) {
            val sessionHelper = SessionHelper(instance)
            sessionHelper.setStringValue(SessionHelper.FIREBASE_TOKEN, token)
            pubnub!!.addPushNotificationsOnChannels()
                .pushType(PNPushType.GCM)
                .channels(Arrays.asList(AppConstants.PUB_SUB_CHANNEL))
                .deviceId(token)
                .async { result: PNPushAddChannelResult?, status: PNStatus ->
                    Log.d(
                        "PUBNUB",
                        "-->PNStatus.getStatusCode = " + status.statusCode
                    )
                    uploadTokenInServer()
                }
        }

        fun uploadTokenInServer(){
            val sessionHelper = SessionHelper(instance)
            if(sessionHelper.getLoginInfo().email.isEmpty()) {
                return
            }
            if(sessionHelper.getStringValue(SessionHelper.UPLOAD_FIREBASE_TOKEN_SUCCESS).isEmpty()) {
                val studentModel = sessionHelper.getLoginInfo()
                studentModel.token = sessionHelper.getStringValue(SessionHelper.FIREBASE_TOKEN)
                studentModel.tokenUpdateTime = currentDataInString()
                val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                val documentReference = db.collection("users").document(sessionHelper.getStringValue(SessionHelper.USER_ID))
                documentReference.set(studentModel).addOnSuccessListener {
                    sessionHelper.setLoginInfo(studentModel)
                    sessionHelper.setStringValue(SessionHelper.UPLOAD_FIREBASE_TOKEN_SUCCESS,"1")
                }.addOnFailureListener { e ->

                }
            }
        }
    }

    private fun initPubnub() {
        try {
            val pnConfiguration = PNConfiguration(UUID.randomUUID().toString().substring(0, 5))
            pnConfiguration.publishKey = "pub-c-06632ca1-cdca-4932-83a5-f05a8088e877"
            pnConfiguration.subscribeKey = "sub-c-181d19e2-1a1e-4ba4-91fd-6ba54a4fc15f"
            pnConfiguration.isSecure = true
            pubnub = PubNub(pnConfiguration)
        } catch (ex: Exception) {
        }
    }
}