package com.example.yrmultimediaco.keepnotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ArchivedNotesActivity extends AppCompatActivity implements ArchivedAdapter.ButtonClickListner{

    FastAdapter<AbstractItem> fastAdapter;
    ItemAdapter<AbstractItem> itemAdapter;
    Toolbar mToolbar;
    RecyclerView archivedRecView;
    DBhelper mDBhelper;
    List<AbstractItem> archivedAdapters = new ArrayList<>();
    TextView noArchivedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_notes);

        mToolbar = findViewById(R.id.archivedToolbar);
        mToolbar.setTitle("Your Archived");

        mDBhelper = new DBhelper(this);

        int colorTitle = ContextCompat.getColor(this, R.color.colorWhite);
        mToolbar.setTitleTextColor(colorTitle);

        setSupportActionBar(mToolbar);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        archivedRecView = findViewById(R.id.archivedNotesRecView);
        archivedRecView.setLayoutManager(new LinearLayoutManager(this));
        archivedRecView.setAdapter(fastAdapter);

        noArchivedText = findViewById(R.id.noArchievedText);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadArchivedNotes();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadArchivedNotes(){

        ArrayList<AbstractItem> archivedNotes = mDBhelper.fetchArchivedNotes();
        archivedAdapters.clear();

        for (AbstractItem item : archivedNotes){

            if (item instanceof ArchivedAdapter){

                ArchivedAdapter archivedNote = (ArchivedAdapter) item;

                ArchivedAdapter archivedAdapter = new ArchivedAdapter(
                        archivedNote.getId(),
                        archivedNote.getTitle(),
                        archivedNote.getSubTitle(),
                        archivedNote.getDescription(),
                        archivedNote.getColor(),
                        archivedNote.getDateTime(),
                        archivedNote.getImagePath()
                );

                archivedAdapter.setButtonClickListner(this);
                archivedAdapters.add(archivedAdapter);

            }

        }

        if (archivedAdapters.isEmpty()){
            noArchivedText.setVisibility(View.VISIBLE);
        } else {
            noArchivedText.setVisibility(View.GONE);
            itemAdapter.add(archivedAdapters);
            fastAdapter.notifyAdapterDataSetChanged();

        }
    }

    @Override
    public void onArchivedButtonClick(int noteId) {     //this is Unarchived button for restoring the archived notes

        mDBhelper.restoreNoteFromArchive(noteId);
        Toast.makeText(this, "You have Unarchived the note ID : " + noteId, Toast.LENGTH_SHORT).show();

        int positionToRemove = -1;
        for (int i = 0; i < archivedAdapters.size(); i++){

            AbstractItem item = archivedAdapters.get(i);
            if (item instanceof ArchivedAdapter && ((ArchivedAdapter) item).getId() == noteId){
                positionToRemove = i;
                break;
            }
        }

        if (positionToRemove != -1){
            archivedAdapters.remove(positionToRemove);
            itemAdapter.set(archivedAdapters);

        }

    }
}