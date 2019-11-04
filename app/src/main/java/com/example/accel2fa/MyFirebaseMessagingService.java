package com.example.accel2fa;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static String TAG = "MyFirebaseSession";
    private static final int SOUND_SIMILARITY_MODE = 1;
    private static final int DISTANCE_VERIFICATION_MODE = 2;

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
        int mode = Integer.parseInt(remoteMessage.getData().get("mode"));

        // Check if message contains a data payload.
        if (mode == SOUND_SIMILARITY_MODE || mode == DISTANCE_VERIFICATION_MODE) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("mode", mode);

            startActivity(intent);
        }
    }
}