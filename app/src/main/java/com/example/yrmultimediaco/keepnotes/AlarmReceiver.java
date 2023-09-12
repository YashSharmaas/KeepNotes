package com.example.yrmultimediaco.keepnotes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, AlarmService.class);
        long noteId = intent.getLongExtra("noteId", -1); // Get the noteId
        String taskTitle = intent.getStringExtra("taskTitle");
        Uri alarmSoundUri = intent.getParcelableExtra("alarmSound");

        if ("dismiss".equals(intent.getAction())) {
            // Trigger the AlarmService to dismiss the notification
            serviceIntent.setAction("dismiss");
            serviceIntent.putExtra("alarmSound", alarmSoundUri);
        } else {
            // Trigger the AlarmService to show the notification
            serviceIntent.setAction("showNotification");
        }

        // Call the method to show the notification
        showNotification(context, noteId ,taskTitle);

        playAlarmSound(context);


        context.startService(serviceIntent);

    }

    public static void cancelAlarm(Context context, long noteId) {
        Intent dismissIntent = new Intent(context, AlarmService.class);
        dismissIntent.setAction("dismiss");
        dismissIntent.putExtra("noteId", noteId);
        context.startService(dismissIntent);
    }

    public void cancelPendingIntentForNoteId(Context context, long noteId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void showNotification(Context context, long noteId, String taskTitle) {
        Intent intent = new Intent(context, MainActivity.class); // Replace with your task detail activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.baseline_notification_add_24)
                .setContentTitle("Alarm: " + taskTitle)
                .setContentText("Your alarm is ringing!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Add the dismiss action to the notification
        Intent dismissIntent = new Intent(context, AlarmService.class);
        dismissIntent.setAction("dismiss");
        dismissIntent.putExtra("noteId", noteId);
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
    }

    private void playAlarmSound(Context context) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmSound);
        if (ringtone != null) {
            ringtone.play();
        }
    }

}
