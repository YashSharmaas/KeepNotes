package com.example.yrmultimediaco.keepnotes;

import static com.example.yrmultimediaco.keepnotes.MainActivity.REQUEST_CODE_AND_NOTE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
//import com.xeoh.android.texthighlighter.TextHighlighter;
@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class CreateNotesActivity extends AppCompatActivity{

    boolean is_storage_image_permitted = false;
    LinearLayout linearMiscellaneous;
    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    ImageView imageBack, addNotes, imageNote;
    ImageView imageColor1;
    ImageView imageColor2;
    ImageView imageColor3;
    ImageView imageColor4;
    ImageView imageColor5;
    EditText noteTitle, noteSubTitle, noteDescription;
    TextView noteDateTime;
    DBhelper mDBhelper;
    CreateNotesAdapter mNotesAdapter;
    int noteId;
    boolean isUpdatingExistingNote = false;
    private View subTitleIndicator;
    private String selectNoteColor;
    private String selectedPathImage;
    private TextView textWebUrl;
    private LinearLayout layoutWebUrl;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private AlertDialog dialogWebUrl;
    private AlertDialog dialogDeleteNote;
    AlertDialog selectionDateTimePicker;
    ImageView imageRemoveImage;
    ImageView imageRemoveUrl;
    ImageView reminderImg;
    private String currentTaskTitle;
    private boolean alarmIsSet = false;
    private long alarmTimeMillis;
    private TextView alarmTimerTextView;
    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String KEY_TIMER_VISIBILITY = "timerVisibility";
    androidx.appcompat.widget.SearchView searchView ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notes);

        searchView = findViewById(R.id.searchTextNote);

        noteTitle = findViewById(R.id.inputNoteTitle);
        noteSubTitle = findViewById(R.id.inputNoteSubTitle);
        noteDescription = findViewById(R.id.inputNoteDescription);
        noteDateTime = findViewById(R.id.textDateTime);
        addNotes = findViewById(R.id.addNotes);
        imageBack = findViewById(R.id.imageBack);
        subTitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebUrl = findViewById(R.id.textWebUrl);
        layoutWebUrl = findViewById(R.id.layoutWebUrl);
        reminderImg = findViewById(R.id.addReminder);
        linearMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        imageRemoveImage = findViewById(R.id.iamgeRemoveImage);
        imageRemoveUrl = findViewById(R.id.imagRemUrl);
        alarmTimerTextView = findViewById(R.id.alarmTimer);

        mDBhelper = new DBhelper(CreateNotesActivity.this);

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        noteDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date())
        );

        selectNoteColor = "#333333";
        selectedPathImage = "";

        reminderImg.setOnClickListener(v -> {

            showDatePicker();

        });

        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.isEmpty()) {
                    // Clear the background color span
                    noteDescription.getText().clearSpans();
                } else {
                    // Highlight the text with a background color span
                    SpannableStringBuilder highlightedText = new SpannableStringBuilder(noteDescription.getText());
                    highlightText(highlightedText, newText);

                    // Set the highlighted text back to the EditText
                    noteDescription.setText(highlightedText);
                }

                return false;
            }
        });

        imageRemoveUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebUrl.setText(null);
                layoutWebUrl.setVisibility(View.GONE);
            }
        });

        imageRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                imageRemoveImage.setVisibility(View.GONE);
                selectedPathImage = "";
            }
        });

        initCustomizations();
        setSubTitleIndicator();

        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        alarmIsSet = preferences.getBoolean("alarmIsSet", false);

        boolean isTimerVisible = getSavedTimerVisibility();
        alarmTimerTextView.setVisibility(isTimerVisible ? View.VISIBLE : View.GONE);


        ImageView cancelReminder = findViewById(R.id.cancelReminder);

        cancelReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alarmIsSet) {
                    // Create an intent to trigger the DismissReceiver
                    Intent dismissIntent = new Intent(getApplicationContext(), DismissReceiver.class);
                    sendBroadcast(dismissIntent);

                    // Display the "removed" toast message
                    String scheduledTime = formatAlarmTime(alarmTimeMillis);
                    String toastMessage = "You have removed the scheduled alarm for " + scheduledTime;
                    Toast.makeText(CreateNotesActivity.this, toastMessage, Toast.LENGTH_SHORT).show();

                    // Cancel the alarm using the noteId
                    cancelAlarm(noteId);

                    // Update the UI and disable the cancelReminder button
                    cancelReminder.setEnabled(false);
                } else {
                    // Display "No alarm is set" toast message
                    Toast.makeText(CreateNotesActivity.this, "No alarm is set", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Bundle extras = getIntent().getExtras();
        if (extras != null){

            boolean isDeletingNote = extras.getBoolean("isDeletingNote", false);

            if (isDeletingNote){
                linearMiscellaneous.findViewById(R.id.layoutDelete).setVisibility(View.VISIBLE);
                linearMiscellaneous.findViewById(R.id.layoutDelete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        showDeletionDialog();
                    }
                });
            }

            noteId = extras.getInt("note_id", -1);

            if (noteId != -1){
                isUpdatingExistingNote = true;
                CreateNotesAdapter existingNote = mDBhelper.getNoteById(noteId);

                if (existingNote != null){
                    noteTitle.setText(existingNote.getTitle());
                    noteSubTitle.setText(existingNote.getSubTitle());
                    noteDescription.setText(existingNote.getDescription());
                    noteDateTime.setText(existingNote.getDateTime());

                    if (layoutWebUrl.getVisibility() == View.VISIBLE){
                        textWebUrl.setText(existingNote.getUrl());
                    }

                    selectNoteColor = existingNote.getColor();

                    if (selectNoteColor.equals("#333333")) {
                        imageColor1.setImageResource(R.drawable.baseline_done_24);
                    } else if (selectNoteColor.equals("#FDBE3B")) {
                        imageColor2.setImageResource(R.drawable.baseline_done_24);
                    } else if (selectNoteColor.equals("#FF4842")) {
                        imageColor3.setImageResource(R.drawable.baseline_done_24);
                    } else if (selectNoteColor.equals("#3A52FC")) {
                        imageColor4.setImageResource(R.drawable.baseline_done_24);
                    } else if (selectNoteColor.equals("#000000")) {
                        imageColor5.setImageResource(R.drawable.baseline_done_24);
                    }

                    selectedPathImage = existingNote.getImagePath();

                }
            }

            String title = extras.getString("note_title", "");
            String subTitle = extras.getString("note_subTitle", "");
            String desc = extras.getString("note_desc", "");
            String url = extras.getString("get_url", "");
            String imagePath = extras.getString("image_path", "");
            String subTitleIndicatorColor = extras.getString("sub_title_indicator_color", "");

            noteTitle.setText(title);
            noteSubTitle.setText(subTitle);
            noteDescription.setText(desc);
            textWebUrl.setText(url);

            if (!url.isEmpty()) {
                layoutWebUrl.setVisibility(View.VISIBLE);
                textWebUrl.setText(url);
            } else {
                layoutWebUrl.setVisibility(View.GONE);
            }

            if (!imagePath.isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    imageNote.setImageBitmap(bitmap);
                    imageNote.setVisibility(View.VISIBLE);
                    imageRemoveImage.setVisibility(View.VISIBLE);
                } else {
                    imageNote.setVisibility(View.GONE);
                }
            } else {
                imageNote.setVisibility(View.GONE);
            }

            if (!subTitleIndicatorColor.isEmpty()){
                GradientDrawable gradientDrawable = (GradientDrawable) subTitleIndicator.getBackground();
                gradientDrawable.setColor(Color.parseColor(subTitleIndicatorColor));
            }

        }

        addNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String titleNote = noteTitle.getText().toString();
                String titleSub = noteSubTitle.getText().toString();
                String descNote = noteDescription.getText().toString();
                String dateTime = noteDateTime.getText().toString();
                String txtWeburl = textWebUrl.getText().toString();

                mNotesAdapter = new CreateNotesAdapter(noteId,
                        titleNote,
                        titleSub,
                        descNote,
                        dateTime,
                        selectNoteColor,
                        selectedPathImage,
                        txtWeburl
                );


                if (titleNote.isEmpty() || titleSub.isEmpty() || descNote.isEmpty()) {
                    Toast.makeText(CreateNotesActivity.this, "Notes can't Empty", Toast.LENGTH_SHORT).show();
                } else {

                    int labelId = getIntent().getIntExtra("labelId", -1);

                    if (isUpdatingExistingNote) {

                        if (mNotesAdapter != null) {
                            if (selectedPathImage.isEmpty()) {
                                selectedPathImage = mNotesAdapter.getImagePath();
                            }
                            if (selectNoteColor.isEmpty()) {
                                selectNoteColor = mNotesAdapter.getColor();
                            }
                        }

                        currentTaskTitle = titleNote;

                        mDBhelper.updateNotes(mNotesAdapter);
                        Toast.makeText(CreateNotesActivity.this, "Notes Updated Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        long newNoteId = mDBhelper.addNotes(mNotesAdapter);

                        // Associate the new note with the label
                        mDBhelper.associateNoteWithLabel((int) newNoteId, labelId);

                        Toast.makeText(CreateNotesActivity.this, "Notes added Successfully", Toast.LENGTH_SHORT).show();
                        noteTitle.setText("");
                        noteSubTitle.setText("");
                        noteDescription.setText("");
                        textWebUrl.setText("");


                    Intent intent = new Intent();
                    intent.putExtra("labelId", labelId);
                    //intent.putExtra("noteId", newNoteId);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            }
        });

        if (getIntent().getBooleanExtra("isFromQuickActions", false)){
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null){
                if (type.equals("image")){
                    selectedPathImage = getIntent().getStringExtra("imagePath");
                    Bitmap imageBitmap = BitmapFactory.decodeFile(selectedPathImage);
                    Log.d("ImagePath", selectedPathImage);
                    Log.d("ImageView", "imageNote: " + (imageNote != null));
                    Log.d("View", "imageRemoveImage: " + (findViewById(R.id.iamgeRemoveImage) != null));

                    imageNote.setImageBitmap(imageBitmap);
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.iamgeRemoveImage).setVisibility(View.VISIBLE);
                } else if (type.equals("URL")) {

                    textWebUrl.setText(getIntent().getStringExtra("URL"));
                    layoutWebUrl.setVisibility(View.VISIBLE);

                }
            }
        }
    }
    /*public void highlightText(EditText et, String textToHighlight) {
        Editable editableText = et.getEditableText();
        String tvt = editableText.toString();
        int ofe = tvt.indexOf(textToHighlight, 0);
        Spannable wordToSpan = new SpannableString(editableText);

        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight, ofs);
            if (ofe == -1)
                break;
            else {
                wordToSpan.setSpan(new BackgroundColorSpan(Color.RED), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        // Update the Editable text with spans
        editableText.replace(0, editableText.length(), wordToSpan);
    }*/

    public void highlightText(Editable editableText, String textToHighlight) {
        String tvt = editableText.toString();
        int ofe = tvt.indexOf(textToHighlight, 0);

        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight, ofs);
            if (ofe == -1)
                break;
            else {
                editableText.setSpan(new BackgroundColorSpan(Color.RED), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }





    private void initCustomizations(){  //initCustomizations is equivalent to initMisellaneous

        bottomSheetBehavior = BottomSheetBehavior.from(linearMiscellaneous);
        linearMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != bottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

         imageColor1 = linearMiscellaneous.findViewById(R.id.image_color1);
         imageColor2 = linearMiscellaneous.findViewById(R.id.image_color2);
         imageColor3 = linearMiscellaneous.findViewById(R.id.image_color3);
         imageColor4 = linearMiscellaneous.findViewById(R.id.image_color4);
         imageColor5 = linearMiscellaneous.findViewById(R.id.image_color5);

linearMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        selectNoteColor = "#333333";
        imageColor1.setImageResource(R.drawable.baseline_done_24);
        imageColor2.setImageResource(0);
        imageColor3.setImageResource(0);
        imageColor4.setImageResource(0);
        imageColor5.setImageResource(0);
        setSubTitleIndicator();
    }
});
linearMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        selectNoteColor = "#FDBE3B";
        imageColor1.setImageResource(0);
        imageColor2.setImageResource(R.drawable.baseline_done_24);
        imageColor3.setImageResource(0);
        imageColor4.setImageResource(0);
        imageColor5.setImageResource(0);
        setSubTitleIndicator();
    }
});
linearMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        selectNoteColor = "#FF4842";
        imageColor1.setImageResource(0);
        imageColor2.setImageResource(0);
        imageColor3.setImageResource(R.drawable.baseline_done_24);
        imageColor4.setImageResource(0);
        imageColor5.setImageResource(0);
        setSubTitleIndicator();
    }
});
linearMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        selectNoteColor = "#3A52FC";
        imageColor1.setImageResource(0);
        imageColor2.setImageResource(0);
        imageColor3.setImageResource(0);
        imageColor4.setImageResource(R.drawable.baseline_done_24);
        imageColor5.setImageResource(0);
        setSubTitleIndicator();
    }
});
linearMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        selectNoteColor = "#000000";
        imageColor1.setImageResource(0);
        imageColor2.setImageResource(0);
        imageColor3.setImageResource(0);
        imageColor4.setImageResource(0);
        imageColor5.setImageResource(R.drawable.baseline_done_24);
        setSubTitleIndicator();
    }
});

        linearMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


            handlePermissions();

            }

        });


linearMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
showAddUrlDialog();
    }
});

if (isUpdatingExistingNote) {
    linearMiscellaneous.findViewById(R.id.layoutDelete).setVisibility(View.VISIBLE);
    linearMiscellaneous.findViewById(R.id.layoutDelete).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showDeletionDialog();
        }
    });
}



    }

    public static String[] storage_permission = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permission_33 = {

            Manifest.permission.READ_MEDIA_IMAGES
    };

    public static String [] permissions(){
        String [] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            p = storage_permission_33;

        } else {
            p = storage_permission;
        }
        return p;
    }

    private void handlePermissions(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), permissions()[0])
                == PackageManager.PERMISSION_GRANTED){
            selectImage();
        }
        else if (ActivityCompat.shouldShowRequestPermissionRationale(CreateNotesActivity.this, permissions()[0])){

            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNotesActivity.this);
            builder.setMessage("This App requires Read Images Permission for add image feature to work as expected.")
                    .setTitle("Permission Required")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    CreateNotesActivity.this,
                                    permissions(),
                                    REQUEST_CODE_STORAGE_PERMISSION);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
builder.show();

        } else{
            ActivityCompat.requestPermissions(CreateNotesActivity.this, permissions(), REQUEST_CODE_STORAGE_PERMISSION);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION ){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions()[0])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("This feature is unavailable because this feature requires permission that you have denied. " +
                                "Please allow Images Permission from settings (Add Images) for proceed further")
                        .setTitle("Permission Required")
                        .setCancelable(false);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);

                        dialog.dismiss();
                    }
                });

                builder.show();
            } else {
                handlePermissions();
            }
        }



    }

    private void setSubTitleIndicator(){
        GradientDrawable gradientDrawable = (GradientDrawable) subTitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectNoteColor));
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); // Specify image MIME type
        imagePickerLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null){
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                imageNote.setImageBitmap(bitmap);
                                imageNote.setVisibility(View.VISIBLE);
                                imageRemoveImage.setVisibility(View.VISIBLE);

                                selectedPathImage = getPathFromUri(selectedImage);

                            } catch (Exception e){
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });


    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null){
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showDatePicker(){

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                showTimePicker(year, month, dayOfMonth);
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void showTimePicker(int year, int monthOfYear, int dayOfMonth){

        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                CreateNotesActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // The selected date and time are now available, you can use them to set the reminder
                        Calendar selectedDateTime = Calendar.getInstance();
                        selectedDateTime.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                        //scheduleAlarm(selectedDateTime.getTimeInMillis(), noteId, currentTaskTitle);
                        alarmTimeMillis = selectedDateTime.getTimeInMillis();

                        // Check if the selected time is in the past
                        if (selectedDateTime.getTimeInMillis() > System.currentTimeMillis()) {

                            currentTaskTitle = noteTitle.getText().toString();
                            Log.e("TimerTest", "currentTaskTitle is : " + currentTaskTitle);
                            // Call a method to handle scheduling the alarm with the selectedDateTime
                            scheduleAlarm(selectedDateTime.getTimeInMillis(), noteId ,currentTaskTitle);
                        } else {
                            // Show an error message indicating that the selected time is in the past
                            Toast.makeText(CreateNotesActivity.this, "Please select a future time.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void scheduleAlarm(long alarmTimeMillis, long noteId, String taskTitle){

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("noteId", noteId);
        intent.putExtra("taskTitle", taskTitle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long currentTimeMillis = System.currentTimeMillis();
        long timeDifferenceMillis = alarmTimeMillis - currentTimeMillis;
        int minutesDifference = (int) (timeDifferenceMillis / (60 * 1000));

        String scheduledTime = formatAlarmTime(alarmTimeMillis);

        alarmTimerTextView.setText("Alarm set for: " + scheduledTime);
        alarmTimerTextView.setVisibility(View.VISIBLE);

        // Schedule the alarm
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);

        alarmIsSet = true;
        saveTimerVisibility(true);

        // Store the updated value of alarmIsSet in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("alarmIsSet", alarmIsSet);
        editor.putString("scheduledTime", scheduledTime);
        editor.apply();

        // Show the Toast message with the calculated minutes
        String toastMessage = "You have scheduled the alarm. It will ring in " + minutesDifference + " minutes.";
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

    }

    private String formatAlarmTime(long alarmTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarmTimeMillis);
        return sdf.format(calendar.getTime());
    }

    private void cancelAlarm(long noteId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Cancel the scheduled alarm
        alarmManager.cancel(pendingIntent);

        alarmTimerTextView.setText("");
        alarmTimerTextView.setVisibility(View.GONE);

        alarmIsSet = false;
        saveTimerVisibility(false);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("alarmIsSet", alarmIsSet);
        editor.apply();

        // Show a toast indicating that the alarm has been removed
        Toast.makeText(this, "Alarm for note " + noteId + " has been removed.", Toast.LENGTH_SHORT).show();
    }

    // Save the timer visibility state to shared preferences
    private void saveTimerVisibility(boolean isVisible) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_TIMER_VISIBILITY, isVisible);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean alarmIsSet = preferences.getBoolean("alarmIsSet", false);

        // Retrieve other necessary data from SharedPreferences if needed

        // Update the text view based on the stored data
        if (alarmIsSet) {
            String scheduledTime = preferences.getString("scheduledTime", "");
            alarmTimerTextView.setText("Alarm set for: " + scheduledTime);
            alarmTimerTextView.setVisibility(View.VISIBLE);
        }

    }

    // Retrieve the timer visibility state from shared preferences
    private boolean getSavedTimerVisibility() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_TIMER_VISIBILITY, false);
    }

    private void showDeletionDialog(){
        if (dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    findViewById(R.id.layoutDeleteContainer)
            );
            builder.setView(view);

            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            CheckBox checkBox = view.findViewById(R.id.checkBox);

            view.findViewById(R.id.textDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (checkBox.isChecked()){
                        mDBhelper.moveNoteToTrash(noteId);
                        Toast.makeText(CreateNotesActivity.this, "Note moved to trash", Toast.LENGTH_SHORT).show();
                    } else {
                        mDBhelper.deleteNotes(noteId);
                        Toast.makeText(CreateNotesActivity.this, "Note deleted permanently", Toast.LENGTH_SHORT).show();
                    }


                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    }

    private void showAddUrlDialog(){
        if (dialogWebUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogWebUrl = builder.create();
            if (dialogWebUrl.getWindow() != null){
                dialogWebUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputUrl = view.findViewById(R.id.inputUrl);
            inputUrl.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(CreateNotesActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()) {
                        Toast.makeText(CreateNotesActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        textWebUrl.setText(inputUrl.getText().toString());
                        layoutWebUrl.setVisibility(View.VISIBLE);
                        dialogWebUrl.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogWebUrl.dismiss();
                }
            });
        }

        dialogWebUrl.show();
    }

}