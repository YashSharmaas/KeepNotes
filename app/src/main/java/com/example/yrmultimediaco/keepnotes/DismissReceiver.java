package com.example.yrmultimediaco.keepnotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationManagerCompat;

public class DismissReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 123;

    @Override
    public void onReceive(Context context, Intent intent) {
        stopAlarmSound(context);

        // Cancel the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void stopAlarmSound(Context context) {
        // Stop the alarm sound (if it's currently playing)
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmSound);
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }


}

