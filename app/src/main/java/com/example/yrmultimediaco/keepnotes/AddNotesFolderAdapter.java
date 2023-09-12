package com.example.yrmultimediaco.keepnotes;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class AddNotesFolderAdapter extends AbstractItem<AddNotesFolderAdapter, AddNotesFolderAdapter.ViewHolder> {

    long id;
    String addFolderLabel;

    public AddNotesFolderAdapter(long id, String addFolderLabel) {
        this.id = id;
        this.addFolderLabel = addFolderLabel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddFolderLabel() {
        return addFolderLabel;
    }

    public void setAddFolderLabel(String addFolderLabel) {
        this.addFolderLabel = addFolderLabel;
    }

    @NonNull
    @Override
    public AddNotesFolderAdapter.ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.add_move_copy_folder_view;
    }

    class ViewHolder extends FastAdapter.ViewHolder {

        TextView folderTxt;
        public ViewHolder(View itemView) {
            super(itemView);

            folderTxt = itemView.findViewById(R.id.textFolderForCopyMove);
        }

        @Override
        public void unbindView(IItem item) {

        }

        @Override
        public void bindView(IItem item, List payloads) {

            AddNotesFolderAdapter adapter = (AddNotesFolderAdapter) item;

            folderTxt.setText((CharSequence) adapter.getAddFolderLabel());

        }
    }

}
