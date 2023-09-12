package com.example.yrmultimediaco.keepnotes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;

public class FolderNotes extends AppCompatActivity {

    Toolbar mToolbar;
    FastAdapter<AbstractItem> fastAdapter;
    ItemAdapter<AbstractItem> itemAdapter;
    RecyclerView categoryLabelRecView;
    DBhelper mDBhelper;
    ImageView createLabelFolder;
    AlertDialog labelDialog;
    List<AbstractItem> loadAddFolderLabel = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_notes);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Add Folder");

        mDBhelper = new DBhelper(FolderNotes.this);

        int colorTitle = ContextCompat.getColor(this, R.color.colorWhite);
        mToolbar.setTitleTextColor(colorTitle);

        setSupportActionBar(mToolbar);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        categoryLabelRecView = findViewById(R.id.notesCategoryFolderRecView);
        categoryLabelRecView.setLayoutManager(new LinearLayoutManager(this));
        categoryLabelRecView.setAdapter(fastAdapter);

        createLabelFolder = findViewById(R.id.imageAddNoteMainLabelInFolder);

        createLabelFolder.setOnClickListener(v -> {

            showLabelDialog();

        });

    }

    private void showLabelDialog() {

        //if (labelDialog == null) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.add_folder_dialog,
                findViewById(R.id.layoutAddFolderContainer)
        );
        builder.setView(view);
        builder.setCancelable(false);

        labelDialog = builder.create();
        if (labelDialog.getWindow() != null) {
            labelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        final EditText inputLabelName = view.findViewById(R.id.inputFolderName);
        inputLabelName.requestFocus();

        view.findViewById(R.id.textAddFolder).setOnClickListener(v -> {

            String labelName = inputLabelName.getText().toString().trim();

            if (inputLabelName.getText().toString().trim().isEmpty()) {
                Toast.makeText(FolderNotes.this, "Enter Label Name First", Toast.LENGTH_SHORT).show();
            } else {

                itemAdapter.clear();

                /*long parentLabelId = mDBhelper.addLabelWithParent(labelName);
                long childLabelId = parentLabelId; // Use the same label ID for child in this example
                mDBhelper.addLabelRelationship(parentLabelId, childLabelId);

                AddLabelAdapter addLabelAdapter = new AddLabelAdapter(parentLabelId, labelName);
                loadAddFolderLabel.add(addLabelAdapter);*/
//
//                loadLabels();
                //fastAdapter.notifyAdapterDataSetChanged();
                labelDialog.dismiss();

            }

        });

        view.findViewById(R.id.textCancelFolder).setOnClickListener(v -> {
            labelDialog.dismiss();
            // Refresh the adapter to reflect the changes
            fastAdapter.notifyAdapterDataSetChanged();

        });

        labelDialog.show();

    }

}