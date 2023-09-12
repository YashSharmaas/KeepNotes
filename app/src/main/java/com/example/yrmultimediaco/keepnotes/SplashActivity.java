package com.example.yrmultimediaco.keepnotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    DBhelper mDBhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mDBhelper = new DBhelper(SplashActivity.this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            new SetShortcutsTask().execute();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 2000);
    }

    private class SetShortcutsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

                // Create a list to hold multiple shortcuts
                List<ShortcutInfo> shortcutInfoList = new ArrayList<>();

// Replace this with your logic to fetch recent or updated notes
                ArrayList<AbstractItem> recentNotes = mDBhelper.fetchRegularNotes(); // Replace with your data retrieval logic

                for (AbstractItem note : recentNotes) {
                    if (note instanceof CreateNotesAdapter){

                        CreateNotesAdapter noteAdapter = (CreateNotesAdapter) note;

                        // Create an intent to open the note's activity with the note's identifier (e.g., note_id)
                        Intent intent = new Intent(SplashActivity.this, CreateNotesActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);

                        // Pass the note identifier and other relevant data as extras
                        intent.putExtra("note_id", noteAdapter.getId()); // Replace with your note's unique identifier
                        intent.putExtra("note_title", noteAdapter.getTitle()); // Replace with your note's title

                        String imagePath = noteAdapter.getImagePath();
                        Bitmap iconBitmap = BitmapFactory.decodeFile(imagePath);

                        if (iconBitmap != null){
                            Icon icon = Icon.createWithBitmap(iconBitmap);

                            // Create a ShortcutInfo for each note with the image as the icon
                            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(SplashActivity.this, "Note" + noteAdapter.getTitle())
                                    .setShortLabel(noteAdapter.getTitle()) // Use the note's title
                                    .setLongLabel("Open Note - " + noteAdapter.getTitle()) // Use the note's title
                                    .setIcon(icon) // Set the loaded image as the icon
                                    .setIntent(intent)
                                    .build();

                            shortcutInfoList.add(shortcutInfo);
                        } else {
                            // Create a ShortcutInfo for each note with the image as the icon
                            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(SplashActivity.this, "Note" + noteAdapter.getTitle())
                                    .setShortLabel(noteAdapter.getTitle()) // Use the note's title
                                    .setLongLabel("Open Note - " + noteAdapter.getTitle()) // Use the note's title
                                    .setIcon(Icon.createWithResource(SplashActivity.this, R.mipmap.ic_launcher))
                                    .setIntent(intent)
                                    .build();

                            shortcutInfoList.add(shortcutInfo);
                        }

                    }

                }

// Set the dynamic shortcuts
                shortcutManager.setDynamicShortcuts(shortcutInfoList);

            }
            return null;
        }
    }
}
