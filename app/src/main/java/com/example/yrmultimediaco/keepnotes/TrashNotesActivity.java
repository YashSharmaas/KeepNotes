package com.example.yrmultimediaco.keepnotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;

public class TrashNotesActivity extends AppCompatActivity implements TrashAdapter.ButtonClickListner{

    FastAdapter<AbstractItem> fastAdapter;
    ItemAdapter<AbstractItem> itemAdapter;
    Toolbar mToolbar;
    RecyclerView trashRecView;
    DBhelper mDBhelper;
    TextView noTxtItem;
    List<AbstractItem> trashAdapters = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash_notes);

        mToolbar = findViewById(R.id.trashToolbar);
        mToolbar.setTitle("Your Trash");

        mDBhelper = new DBhelper(TrashNotesActivity.this);

        int colorTitle = ContextCompat.getColor(this, R.color.colorWhite);
        mToolbar.setTitleTextColor(colorTitle);

        setSupportActionBar(mToolbar);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        trashRecView = findViewById(R.id.trashNotesRecView);
        trashRecView.setLayoutManager(new LinearLayoutManager(this));
        trashRecView.setAdapter(fastAdapter);

        noTxtItem = findViewById(R.id.noTrashText);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadTrashedNotes();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){

           onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadTrashedNotes() {
        ArrayList<AbstractItem> trashedNotes = mDBhelper.fetchTrashedNotes();
        trashAdapters.clear();

        for (AbstractItem item : trashedNotes) {

            if (item instanceof TrashAdapter){
                TrashAdapter trashedNote = (TrashAdapter) item;


                TrashAdapter trashAdapter = new TrashAdapter(
                        trashedNote.getId(),
                        trashedNote.getTitle(),
                        trashedNote.getSubTitle(),
                        trashedNote.getDescription(),
                        trashedNote.getColor(),
                        trashedNote.getDateTime(),
                        trashedNote.getImagePath()
                );

                trashAdapter.setButtonClickListner(this);
                trashAdapters.add(trashAdapter);

            }

        }

        if (trashAdapters.isEmpty()) {
            trashRecView.setVisibility(View.GONE);
            noTxtItem.setVisibility(View.VISIBLE);
        } else {
            trashRecView.setVisibility(View.VISIBLE);
            noTxtItem.setVisibility(View.GONE);
            itemAdapter.set(trashAdapters); // Set trashed notes to the itemAdapter
            fastAdapter.notifyAdapterDataSetChanged(); // Notify adapter about the data change
        }
    }

    @Override
    public void onRestoreButtonClick(int noteId) {
        mDBhelper.restoreNoteFromTrash(noteId);
        Toast.makeText(this, "Note Restored " + noteId, Toast.LENGTH_SHORT).show();
        //loadTrashedNotes();

        int positionToRemove = -1;
        for (int i = 0; i < trashAdapters.size(); i++) {
            AbstractItem item = trashAdapters.get(i);
            if (item instanceof TrashAdapter && ((TrashAdapter) item).getId() == noteId){
                positionToRemove = i;
                break;
            }
        }

        // Remove the restored item from the list
        if (positionToRemove != -1) {
            trashAdapters.remove(positionToRemove);
            itemAdapter.set(trashAdapters); // Update the adapter
        }
    }

    @Override
    public void onDeleteButtonClick(int noteId) {

        mDBhelper.deleteNotes(noteId);
        Toast.makeText(this, "Note Deleted " + noteId, Toast.LENGTH_SHORT).show();
        //loadTrashedNotes();
        int positionToRemove = -1;
        for (int i = 0; i < trashAdapters.size(); i++) {
            AbstractItem item = trashAdapters.get(i);
            if (item instanceof TrashAdapter && ((TrashAdapter) item).getId() == noteId){
                positionToRemove = i;
                break;
            }
        }

        // Remove the restored item from the list
        if (positionToRemove != -1) {
            trashAdapters.remove(positionToRemove);
            itemAdapter.set(trashAdapters); // Update the adapter
        }
    }
}