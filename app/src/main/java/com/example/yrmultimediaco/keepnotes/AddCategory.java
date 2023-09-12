package com.example.yrmultimediaco.keepnotes;

import static com.example.yrmultimediaco.keepnotes.MainActivity.REQUEST_CODE_AND_NOTE;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddCategory extends AppCompatActivity implements CreateNotesAdapter.OnBtnDetailClickListener {

    Toolbar mToolbar;
    DBhelper mDBhelper;
    FastAdapter<AbstractItem> fastAdapter;
    ItemAdapter<AbstractItem> itemAdapter;
    RecyclerView notesCategoryRecView;
    ImageView imageAddNoteInLabel;
    AlertDialog removeNoteFromLabel;
    AlertDialog renameLabelDialog;
    AlertDialog labelDialog;
    private ArrayList<AbstractItem> labelList = new ArrayList<>();
    private HashMap<Long, List<AbstractItem>> labelHierarchy = new HashMap<>();
    private FloatingActionButton fabButton;
    private int labelId;
    private boolean isMoveOperation;
    private boolean isCopyOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        mToolbar = findViewById(R.id.toolbar);

        int colorTitle = ContextCompat.getColor(this, R.color.colorWhite);
        mToolbar.setTitleTextColor(colorTitle);

        String labelName = getIntent().getStringExtra("labelName");

        mToolbar.setTitle(labelName);

        setSupportActionBar(mToolbar);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        notesCategoryRecView = findViewById(R.id.notesAllCategoryRecView);
        notesCategoryRecView.setLayoutManager(new LinearLayoutManager(this));
        notesCategoryRecView.setAdapter(fastAdapter);
        fabButton = findViewById(R.id.fabCopyMove);

        mDBhelper = new DBhelper(AddCategory.this);

        // Retrieve the isMoveOperation value from the intent
        isMoveOperation = getIntent().getBooleanExtra("isMoveOperation", false);
        isCopyOperation = getIntent().getBooleanExtra("isCopyOperation", false);

        if (getIntent() != null) {

            if (isMoveOperation) {
                fabButton.setVisibility(View.VISIBLE);
                fabButton.setImageResource(R.drawable.baseline_drive_file_move_24);
            } else if (isCopyOperation){
                fabButton.setVisibility(View.VISIBLE);
                fabButton.setImageResource(R.drawable.baseline_copy_all_24);
            } else {
                fabButton.setVisibility(View.GONE);
            }
        }

        labelId = getIntent().getIntExtra("labelId", -1);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int noteId = getIntent().getIntExtra("noteId", -1);

                if (noteId != -1) {
                    // Check if the selected note is already associated with the label
                    boolean isNoteAlreadyAssociated = mDBhelper.isNoteAssociatedWithLabel(noteId, labelId);
                    //boolean isMoveOperation = getIntent().getBooleanExtra("isMoveOperation", false);

                    if (!isNoteAlreadyAssociated) {
                        if (isMoveOperation) {
                            // Moving the note
                            mDBhelper.disassociateNoteFromLabels(noteId); // Disassociate from current labels
                            mDBhelper.associateNoteWithLabel(noteId, labelId); // Associate with the chosen label
                            mDBhelper.moveNoteToTrash(noteId); // Move note to trash
                            Toast.makeText(AddCategory.this, "Note moved to the label", Toast.LENGTH_SHORT).show();
                        } else {
                            // Copying the note
                            mDBhelper.associateNoteWithLabel(noteId, labelId); // Associate with the chosen label
                            Toast.makeText(AddCategory.this, "Note copied with the label", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Show toast indicating that the note is already associated with the label
                        Toast.makeText(AddCategory.this, "Note already associated with this label", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imageAddNoteInLabel = findViewById(R.id.imageAddNoteMainData);

        imageAddNoteInLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNotesActivity.class);

                int labelId = getIntent().getIntExtra("labelId", -1);

                if (labelId != -1) {
                    intent.putExtra("labelId", labelId); // Pass the labelId to CreateNotesActivity
                }
                startActivityForResult(intent, REQUEST_CODE_AND_NOTE);

            }
        });

        fastAdapter.withOnClickListener(new OnClickListener<AbstractItem>() {
            @Override
            public boolean onClick(View v, IAdapter<AbstractItem> adapter, AbstractItem item, int position) {

                if (item instanceof CreateNotesAdapter) {
                    CreateNotesAdapter notesAdapter = (CreateNotesAdapter) item;

                    Intent intent = new Intent(AddCategory.this, CreateNotesActivity.class);

                    intent.putExtra("isDeletingNote", true);

                    intent.putExtra("note_id", notesAdapter.getId());
                    intent.putExtra("note_title", notesAdapter.getTitle());
                    intent.putExtra("note_subTitle", notesAdapter.getSubTitle());
                    intent.putExtra("note_desc", notesAdapter.getDescription());
                    intent.putExtra("get_url", notesAdapter.getUrl());
                    intent.putExtra("image_path", notesAdapter.getImagePath());
                    intent.putExtra("sub_title_indicator_color", notesAdapter.getColor());

                    startActivityForResult(intent, REQUEST_CODE_AND_NOTE);


                }
                return false;
            }
        });

        getLabelNotes();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void getLabelNotes(){

        int labelId = getIntent().getIntExtra("labelId", -1);
        //int noteId = getIntent().getIntExtra("noteId", -1);
        int noteId = -1;

        if (getIntent().hasExtra("noteId")) {
            noteId = getIntent().getIntExtra("noteId", -1);
        }

        itemAdapter.clear();

        if (labelId != -1) {

            if (noteId != -1) {
                // Fetch the selected note's data
                CreateNotesAdapter selectedNote = mDBhelper.getNoteById(noteId);

                // Fetch the notes associated with the selected label
                ArrayList<AbstractItem> notesForLabel = mDBhelper.fetchNotesForLabel(labelId);

                boolean isNoteAlreadyAssociated = false;
                for (AbstractItem item : notesForLabel) {
                    if (item instanceof CreateNotesAdapter) {
                        CreateNotesAdapter note = (CreateNotesAdapter) item;

                        if (note.getId() == selectedNote.getId()) {

                            isNoteAlreadyAssociated = true;
                            if (note.getBtnDetailClickListener() == null) {
                                note.setBtnDetailClickListener(this);
                            }
                            break;
                        }
                    }
                }

                if (!isNoteAlreadyAssociated) {
                    // Add the selected note to the list
                    notesForLabel.add(selectedNote);
                    if (!notesForLabel.isEmpty()) {
                        itemAdapter.add(notesForLabel);
                    } else {
                        Toast.makeText(this, "No notes associated with this label", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show toast indicating that the note is already associated with the label
                    Toast.makeText(this, "Note already associated with this label", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Fetch and display notes associated with the label only
                ArrayList<AbstractItem> notesForLabel = mDBhelper.fetchNotesForLabel(labelId);
                itemAdapter.add(notesForLabel);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.label_menu, menu);

        MenuItem renameLabel = menu.findItem(R.id.labelRename);
        MenuItem deleteLabel = menu.findItem(R.id.labelDelete);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){

            onBackPressed();
        } else if (item.getItemId() == R.id.labelRename) {

            int labelId = getIntent().getIntExtra("labelId", -1);
            if (labelId != -1) {
                renameLabel(labelId); // Call the renaming function
            }

        } else if (item.getItemId() == R.id.labelDelete) {

            int labelId = getIntent().getIntExtra("labelId", -1);
            if (labelId != -1) {
                deleteLabelWithNotes(labelId); // Call the delete function
            }
        } /*else if (item.getItemId() == R.id.labelAdd) {

            int labelId = getIntent().getIntExtra("labelId", -1);
            if (labelId != -1) {
                showSubLabelDialog(labelId); // Call the sub-label creation function
            }
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    private void deleteLabelWithNotes(int labelId) {
        mDBhelper.deleteLabelAndAssociatedNotes(labelId);
        // You can navigate back to the previous activity or perform other actions
        //onBackPressed();
        Toast.makeText(this, "Label Deleted", Toast.LENGTH_SHORT).show();


        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showSubLabelDialog(final long parentLabelId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.add_label_dialog,
                findViewById(R.id.layoutAddLabelContainer)
        );
        builder.setView(view);
        builder.setCancelable(false);

        final EditText inputLabelName = view.findViewById(R.id.inputLabelName);
        inputLabelName.requestFocus();

        view.findViewById(R.id.textAddLabel).setOnClickListener(v -> {
            String labelName = inputLabelName.getText().toString().trim();

            if (labelName.isEmpty()) {
                Toast.makeText(AddCategory.this, "Enter Label Name First", Toast.LENGTH_SHORT).show();
            } else {
                long subLabelId = mDBhelper.addSubLabel(labelName, parentLabelId);

                List<AbstractItem> items = new ArrayList<>();
                items.add(new AddLabelAdapter(subLabelId, labelName));
                // You might need to fetch and add associated notes here
//                labelData.put(parentLabelId, items);
//
//                LabelItem subLabel = new LabelItem(subLabelId, labelName);
                //labelHierarchy.computeIfAbsent(parentLabelId, k -> new ArrayList<>()).add(subLabel);
                // Fetch and add associated notes to subLabel here


                labelDialog.dismiss();
                // Notify your adapter that the data has changed
                fastAdapter.notifyDataSetChanged();
            }
        });

        view.findViewById(R.id.textCancelLabel).setOnClickListener(v -> {
            labelDialog.dismiss();
        });

        labelDialog = builder.create();
        if (labelDialog.getWindow() != null) {
            labelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        labelDialog.show();
    }


    private void renameLabel(int labelId) {

        String currentLabelName = mDBhelper.getLabelNameById(labelId);

//        if (renameLabelDialog == null){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.rename_label,
                    findViewById(R.id.layoutRenameLabelContainer)
            );
            builder.setView(view);

            renameLabelDialog = builder.create();
            if (renameLabelDialog.getWindow() != null) {
                renameLabelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            EditText inputLabelName = view.findViewById(R.id.inputLabelName);
            inputLabelName.setText(currentLabelName);

            view.findViewById(R.id.textRenameLabel).setOnClickListener(v -> {

                String newLabelName = inputLabelName.getText().toString().trim();
                if (!newLabelName.isEmpty()) {
                    mDBhelper.updateLabelName(labelId, newLabelName);

                    getLabelNotes();
                    Toast.makeText(this, "Label Renamed", Toast.LENGTH_SHORT).show();

                    renameLabelDialog.dismiss();
                }

            });

            view.findViewById(R.id.textCancelLabel).setOnClickListener(v -> {
                renameLabelDialog.dismiss();
                // Refresh the adapter to reflect the changes
                fastAdapter.notifyAdapterDataSetChanged();

            });

            renameLabelDialog.show();

        }

   // }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AND_NOTE && resultCode == RESULT_OK) {
            getLabelNotes();
            int labelId = data.getIntExtra("labelId", -1);
            int newNoteId = (int) data.getLongExtra("noteId", -1);

            if (labelId != -1 && newNoteId != -1) {

                mDBhelper.associateNoteWithLabel(newNoteId, labelId);


            }

            boolean isMoveOperation = data.getBooleanExtra("isMoveOperation", false);

            if (isMoveOperation) {
                fabButton.setVisibility(View.VISIBLE);
                fabButton.setImageResource(R.drawable.baseline_drive_file_move_24);
            } else {
                fabButton.setVisibility(View.VISIBLE);
                fabButton.setImageResource(R.drawable.baseline_copy_all_24);
            }

        }
    }

    @Override
    public void onBtnDetailsClicked(int itemId) {
        Log.d("AddCategory", "onBtnDetailsClicked called for itemId: " + itemId);

        int labelId = getIntent().getIntExtra("labelId", -1);

        if (labelId != -1) {
            mDBhelper.deleteNoteFromLabel(itemId, labelId);
            getLabelNotes(); // Refresh the list after deleting the note
        }

        // Show a toast to confirm the button click
        Toast.makeText(AddCategory.this, "You tapped on: " + labelId + " with " + itemId, Toast.LENGTH_SHORT).show();
    }

}