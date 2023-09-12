package com.example.yrmultimediaco.keepnotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShortcutReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("com.android.launcher.action.INSTALL_SHORTCUT")) {
            // Handle the shortcut creation logic here
            String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
            if (shortcutName != null && shortcutName.equals("Note Shortcut")) {
                // This is your "Note Shortcut," you can perform additional actions if needed
                // For example, you can show a confirmation message or log the creation.
                Log.d("ShortcutReceiver", "Shortcut created: " + shortcutName);
                // You can also perform any other actions specific to this shortcut.
            }
        }
    }
}

