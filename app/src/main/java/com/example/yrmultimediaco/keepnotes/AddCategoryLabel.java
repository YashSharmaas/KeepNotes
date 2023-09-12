package com.example.yrmultimediaco.keepnotes;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.ArrayList;
import java.util.List;

public class AddCategoryLabel extends AppCompatActivity implements AddLabelAdapter.ButtonClickListner {

    Toolbar mToolbar;
    FastAdapter<AbstractItem> fastAdapter;
    ItemAdapter<AbstractItem> itemAdapter;
    RecyclerView categoryLabelRecView;
    DBhelper mDBhelper;
    ImageView createLabel;
    AlertDialog labelDialog;
    List<AbstractItem> loadAddLabel = new ArrayList<>();
    public static final int REQUEST_CODE_AND_NOTE = 2;
    TextView noLabelText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category_label);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Add Label");

        mDBhelper = new DBhelper(AddCategoryLabel.this);

        int colorTitle = ContextCompat.getColor(this, R.color.colorWhite);
        mToolbar.setTitleTextColor(colorTitle);

        setSupportActionBar(mToolbar);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        categoryLabelRecView = findViewById(R.id.notesCategoryRecView);
        categoryLabelRecView.setLayoutManager(new LinearLayoutManager(this));
        categoryLabelRecView.setAdapter(fastAdapter);

        noLabelText = findViewById(R.id.noLabelText);

        createLabel = findViewById(R.id.imageAddNoteMainLabel);

        createLabel.setOnClickListener(v -> {

            showLabelDialog();

        });
        boolean isMoveOperation = getIntent().getBooleanExtra("isMoveOperation", false);
        boolean isCopyOperation = getIntent().getBooleanExtra("isCopyOperation", false);


        fastAdapter.withOnClickListener(new OnClickListener<AbstractItem>() {
            @Override
            public boolean onClick(View v, IAdapter<AbstractItem> adapter, AbstractItem item, int position) {
                if (item instanceof AddLabelAdapter) {
                    AddLabelAdapter labelAdapter = (AddLabelAdapter) item;
                    int labelId = (int) labelAdapter.getId();
                    String labelName = labelAdapter.getAddLabel();
                    int noteId = getIntent().getIntExtra("noteId", -1);
                    Intent intent = new Intent(AddCategoryLabel.this, AddCategory.class);
                    intent.putExtra("labelId", labelId);
                    intent.putExtra("noteId", noteId);
                    intent.putExtra("labelName", labelName);
                    intent.putExtra("isMoveOperation", isMoveOperation);
                    intent.putExtra("isCopyOperation", isCopyOperation);

                    //startActivity(intent);
                    startActivityForResult(intent, REQUEST_CODE_AND_NOTE);
                }
                return false;
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadLabels();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){

            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLabelDialog() {

        //if (labelDialog == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.add_label_dialog,
                    findViewById(R.id.layoutAddLabelContainer)
            );
            builder.setView(view);
            builder.setCancelable(false);

            labelDialog = builder.create();
            if (labelDialog.getWindow() != null) {
                labelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputLabelName = view.findViewById(R.id.inputLabelName);
            inputLabelName.requestFocus();

            view.findViewById(R.id.textAddLabel).setOnClickListener(v -> {

                String labelName = inputLabelName.getText().toString().trim();

                if (inputLabelName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(AddCategoryLabel.this, "Enter Label Name First", Toast.LENGTH_SHORT).show();
                } else {

                    itemAdapter.clear();

                    long labelId = mDBhelper.addLabel(labelName);
                    AddLabelAdapter addLabelAdapter = new AddLabelAdapter(labelId, labelName);
                    loadAddLabel.add(addLabelAdapter);

                    loadLabels();
                    //fastAdapter.notifyAdapterDataSetChanged();
                    labelDialog.dismiss();

                }

            });

            view.findViewById(R.id.textCancelLabel).setOnClickListener(v -> {
                labelDialog.dismiss();
                // Refresh the adapter to reflect the changes
                fastAdapter.notifyAdapterDataSetChanged();

            });

            labelDialog.show();

        }

    //}

    private void loadLabels() {

        ArrayList<AbstractItem> loadlabel = mDBhelper.getAllLabels();
        loadAddLabel.clear();

        if (loadlabel.isEmpty()){
            noLabelText.setVisibility(View.VISIBLE);
        }else {
            noLabelText.setVisibility(View.GONE);
        }

        for (AbstractItem item : loadlabel) {

            if (item instanceof AddLabelAdapter) {

                AddLabelAdapter labelAdapter = (AddLabelAdapter) item;

                AddLabelAdapter addLabelAdapter = new AddLabelAdapter(
                        labelAdapter.getId(),
                        labelAdapter.getAddLabel()
                );

                loadAddLabel.add(addLabelAdapter);

                addLabelAdapter.setButtonClickListner(this);
            }

        }


        itemAdapter.add(loadAddLabel);
        fastAdapter.notifyAdapterDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AND_NOTE && resultCode == RESULT_OK) {
            loadLabels();

            // Notify the adapter that the data has changed
            fastAdapter.notifyAdapterDataSetChanged();

        }
    }

        @Override
    public void checkBoxClick(int labelId) {
        int noteId = getIntent().getIntExtra("noteId", -1);

        if (noteId != -1) {
            // Check if the selected note is already associated with the label
            boolean isNoteAlreadyAssociated = mDBhelper.isNoteAssociatedWithLabel(noteId, labelId);
            boolean isMoveOperation = getIntent().getBooleanExtra("isMoveOperation", false);

            if (!isNoteAlreadyAssociated) {
                if (isMoveOperation) {
                    // Moving the note
                    mDBhelper.disassociateNoteFromLabels(noteId); // Disassociate from current labels
                    mDBhelper.associateNoteWithLabel(noteId, labelId); // Associate with the chosen label
                    mDBhelper.moveNoteToTrash(noteId); // Move note to trash
                    Toast.makeText(this, "Note moved to the label", Toast.LENGTH_SHORT).show();
                } else {
                    // Copying the note
                    mDBhelper.associateNoteWithLabel(noteId, labelId); // Associate with the chosen label
                    Toast.makeText(this, "Note copied with the label", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Show toast indicating that the note is already associated with the label
                Toast.makeText(this, "Note already associated with this label", Toast.LENGTH_SHORT).show();
            }
        }
    }


}