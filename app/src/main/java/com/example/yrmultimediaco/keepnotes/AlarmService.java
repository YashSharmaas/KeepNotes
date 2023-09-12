package com.example.yrmultimediaco.keepnotes;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            long noteId = intent.getLongExtra("noteId", -1); // Get the noteId
            String taskTitle = intent.getStringExtra("taskTitle");
            Uri alarmSoundUri = intent.getParcelableExtra("alarmSound");

            String action = intent.getAction();

            if ("dismiss".equals(action)) {
                // Handle dismiss action
                dismissNotification(noteId);
                stopAlarmSound(alarmSoundUri);
            }

            // Rest of your code for handling different actions
        } else {
            // Play the alarm sound and show the notification
//            playAlarmSound(this);
//            showNotification(this, noteId ,taskTitle);
        }


        // Return START_STICKY to ensure the service restarts if killed by the system
        return START_STICKY;
    }

   /* private void playAlarmSound(Context context) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmSound);
        if (ringtone != null) {
            ringtone.play();
        }
    }

    private void showNotification(Context context, long noteId, String taskTitle) {
        Intent intent = new Intent(context, MainActivity.class); // Replace with your task detail activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.baseline_notification_add_24)
                .setContentTitle("Alarm: " + taskTitle)
                .setContentText("Your alarm is ringing!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Intent dismissIntent = new Intent(context, AlarmService.class);
        dismissIntent.setAction("dismiss");
        PendingIntent dismissPendingIntent = PendingIntent.getService(
                context,
                0,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.addAction(R.drawable.baseline_alarm_off_24, "Dismiss", dismissPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) noteId, builder.build()); // Use a unique ID for each notification
    }*/

    private void dismissNotification(long noteId) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel((int) noteId);
        Toast.makeText(this, "You dissmiss the id of that alarm" + noteId, Toast.LENGTH_SHORT).show();
        // Stop the service
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopAlarmSound(Uri alarmSoundUri) {
        if (alarmSoundUri != null) {
            Ringtone ringtone = RingtoneManager.getRingtone(this, alarmSoundUri);
            if (ringtone != null) {
                if (ringtone.isPlaying()) {
                    ringtone.stop();
                }
            }
        }
    }

}



