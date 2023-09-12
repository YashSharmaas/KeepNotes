package com.example.yrmultimediaco.keepnotes;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NotesDetailBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String ITEM_ID = "bottomSheetTag";

    private int itemId;
    View view;
    private NotesDetailBottomSheetFragmentListener mListener;

    public interface NotesDetailBottomSheetFragmentListener {
        void onNotesOptionSelected(int idOption);
    }
    public NotesDetailBottomSheetFragment() {
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public static NotesDetailBottomSheetFragment newInstance(int itemId){
        NotesDetailBottomSheetFragment notesDetailBottomSheetFragment = new NotesDetailBottomSheetFragment();
        Bundle arg = new Bundle();
        arg.putInt(ITEM_ID, itemId);
        notesDetailBottomSheetFragment.setArguments(arg);
        return notesDetailBottomSheetFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), getTheme());
        view = LayoutInflater.from(requireContext()).inflate(R.layout.more_options_layout, null);
        dialog.setContentView(view);

        LinearLayout layoutEdit = view.findViewById(R.id.layoutEdit);
        LinearLayout layoutShare = view.findViewById(R.id.layoutShare);
        LinearLayout layoutDelete = view.findViewById(R.id.layoutDeletee);
        LinearLayout layoutFav = view.findViewById(R.id.layoutFavorites);
        LinearLayout layoutAddCategory = view.findViewById(R.id.layoutAddToCategory);
        LinearLayout layoutDuplication = view.findViewById(R.id.layoutDuplicateNote);
        LinearLayout layoutArchiveNote = view.findViewById(R.id.layoutArchievedNote);
        LinearLayout layoutSaveNoteAsText = view.findViewById(R.id.layoutSaveNote);
        LinearLayout layoutCopyContent = view.findViewById(R.id.copyToClipBoard);
        LinearLayout layoutLockNote = view.findViewById(R.id.layoutLockNote);
        LinearLayout layoutMoveCategory = view.findViewById(R.id.layoutMoveToCategory);
        //LinearLayout layoutShortcutNote = view.findViewById(R.id.layoutShortcutNote);


        layoutEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onNotesOptionSelected(R.id.layoutEdit);
                }
                dismiss();
            }
        });

        layoutShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onNotesOptionSelected(R.id.layoutShare);
                }
                dismiss();
            }
        });

        layoutDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onNotesOptionSelected(R.id.layoutDeletee);
                }
                dismiss();
            }
        });

        layoutFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.layoutFavorites);
                }
                dismiss();
            }
        });

        layoutAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.layoutAddToCategory);
                }
                dismiss();
            }
        });

        layoutDuplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.layoutDuplicateNote);
                }
                dismiss();
            }
        });

        layoutArchiveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.layoutArchievedNote);
                }
                dismiss();
            }
        });

        layoutSaveNoteAsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.layoutSaveNote);
                }
                dismiss();
            }
        });

        layoutCopyContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.copyToClipBoard);
                    dismiss();
                }
            }
        });

        layoutLockNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.layoutLockNote);
                    dismiss();
                }
            }
        });

        layoutMoveCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.layoutMoveToCategory);
                    dismiss();
                }
            }
        });

        /*layoutShortcutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onNotesOptionSelected(R.id.layoutShortcutNote);
                    dismiss();
                }
            }
        });*/

        return dialog;
    }


    public void setListener(NotesDetailBottomSheetFragmentListener listener) {
        mListener = listener;
    }

}
