package com.example.yrmultimediaco.keepnotes;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;

public class AddLabelAdapter extends AbstractItem<AddLabelAdapter, AddLabelAdapter.ViewHolder> {

    long id;
    String addLabel;
    DBhelper mDBhelper;
    private AddLabelAdapter.ButtonClickListner mButtonClickListner;

    public AddLabelAdapter(long id, String addLabel) {
        this.id = id;
        this.addLabel = addLabel;
    }

    public interface ButtonClickListner{

        void checkBoxClick(int noteId);

    }

    public void setButtonClickListner(ButtonClickListner buttonClickListner) {
        mButtonClickListner = buttonClickListner;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddLabel() {
        return addLabel;
    }

    public void setAddLabel(String addLabel) {
        this.addLabel = addLabel;
    }

    @NonNull
    @Override
    public AddLabelAdapter.ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.add_category_label_view;
    }

    class ViewHolder extends FastAdapter.ViewHolder {

        TextView labelTxt;
        CheckBox mCheckBox;
        DBhelper mDBhelper;

        public ViewHolder(View itemView) {
            super(itemView);

            labelTxt = itemView.findViewById(R.id.textFolderLabel);
            mCheckBox = itemView.findViewById(R.id.checkBox);

            mDBhelper = new DBhelper(itemView.getContext());

        }

        @Override
        public void unbindView(IItem item) {

        }

        @Override
        public void bindView(IItem item, List payloads) {

            AddLabelAdapter addLabelAdapter = (AddLabelAdapter) item;

            labelTxt.setText((CharSequence) addLabelAdapter.getAddLabel());

            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCheckBox.isChecked() && mButtonClickListner != null) {
                        mButtonClickListner.checkBoxClick((int) addLabelAdapter.getId());
                    }
                }
            });


        }
    }}
