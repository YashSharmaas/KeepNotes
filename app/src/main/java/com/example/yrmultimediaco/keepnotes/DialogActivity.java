package com.example.yrmultimediaco.keepnotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DialogActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Note")
                .setMessage("You clicked the add note image in the widget.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent createNotesIntent = new Intent(DialogActivity.this, CreateNotesActivity.class);
                        createNotesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(createNotesIntent);

                        dialog.dismiss();
                        finish(); // Close the DialogActivity
                    }
                });
        builder.create().show();


    }
}
