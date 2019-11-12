package com.example.sd2fa;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static String TAG = "MyFirebaseSession";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // Logic to register token
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        int startFreq = Integer.parseInt(remoteMessage.getData().get("start_freq"));
        int endFreq = Integer.parseInt(remoteMessage.getData().get("end_freq"));

        // Check if message contains a data payload.
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("start_freq", startFreq);
        intent.putExtra("end_freq", endFreq);

        startActivity(intent);
    }
}