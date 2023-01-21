package com.example.comptutor.utils;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseService";
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG,"NEW_TOKEN: "+token);
        ComptutorApplication.Companion.sendRegistrationToPubNub(token);
//        sendRegistrationToPubNub(token);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try{
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            }
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }

            Map<String, String> dataMap = remoteMessage.getData();
            String data = dataMap.get("data");

            EventBus.getDefault().post(new NotificationEvent(data));
        }catch (Exception e) {

        }
    }
}
