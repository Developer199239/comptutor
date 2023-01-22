package com.example.comptutor.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.comptutor.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.push.PNPushRemoveChannelResult;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseService";
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG,"NEW_TOKEN: "+token);

        SessionHelper sessionHelper = new SessionHelper(getApplicationContext());
        if(!sessionHelper.getStringValue(SessionHelper.FIREBASE_TOKEN).isEmpty()) {
            ComptutorApplication.Companion.getPubnub().removePushNotificationsFromChannels()
                    .pushType(PNPushType.FCM)
                    .deviceId(sessionHelper.getStringValue(SessionHelper.FIREBASE_TOKEN))
                    .channels(Arrays.asList(AppConstants.PUB_SUB_CHANNEL))
                    .async((result, status) -> ComptutorApplication.Companion.sendRegistrationToPubNub(token));
        } else {
            ComptutorApplication.Companion.sendRegistrationToPubNub(token);
        }
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

            SessionHelper sessionHelper = new SessionHelper(getApplicationContext());
            if(sessionHelper.getLoginInfo().getRole().equals(AppConstants.ROLE_TEACHER)) {
                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.blip);
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.start();
            }
        }catch (Exception e) {

        }
    }
}
