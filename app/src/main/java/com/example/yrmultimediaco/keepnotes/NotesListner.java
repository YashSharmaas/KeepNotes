package com.example.yrmultimediaco.keepnotes;

public interface NotesListner {
    void onUpdateNote(CreateNotesAdapter noteAdapter, boolean showDeleteLayout);
    void onDeleteNote(int noteId);
}
