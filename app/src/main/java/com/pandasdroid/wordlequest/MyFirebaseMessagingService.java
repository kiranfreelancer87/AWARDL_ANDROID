package com.pandasdroid.wordlequest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";
    private static final String CHANNEL_ID = "my_channel_id"; // Notification channel ID

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed Token: " + token);
        saveToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Display the notification
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            displayNotification(title, body);
        }
    }

    private void saveToken(String token) {
        // Save the token to a shared preference or database for later use
        // Example: SharedPreferences or Firebase Realtime Database
        // Your implementation here
        Log.wtf("Token...", token);
        getSharedPreferences(GameConstants.Pref_Name, Context.MODE_PRIVATE).edit().putString("firebase_token", "").apply();
    }

    private void displayNotification(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create a notification channel for Android Oreo and above
        {
            CharSequence channelName = "WORDLE_CH_1";
            String channelDescription = "WORDLE_NOTIFICATIONS_AND_EVENTS_HANDLER";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Display the notification
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(0, builder.build());
    }
}
