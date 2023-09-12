package com.example.yrmultimediaco.keepnotes;


import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.makeramen.roundedimageview.RoundedImageView;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter_extensions.drag.IDraggable;

import java.util.ArrayList;
import java.util.List;

public class CreateNotesAdapter extends AbstractItem<CreateNotesAdapter, CreateNotesAdapter.ViewHolder> implements IItem<CreateNotesAdapter, CreateNotesAdapter.ViewHolder>, IDraggable{

    int id;
    String title, subTitle, description, dateTime, imagePath;
    String color;
    String url;
    private boolean isSelected = false;
    boolean isFavorite = false;
    private boolean isNoteLock = false;

    public boolean isNoteLock() {
        return isNoteLock;
    }

    public void setNoteLock(boolean noteLock) {
        isNoteLock = noteLock;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty();
    }

    public boolean hasUrl() {
        return url != null && !url.isEmpty();
    }
    public boolean hasColor(){
        return color != null && !color.isEmpty();
    }


    public CreateNotesAdapter(int id, String title, String subTitle, String description, String dateTime, String color, String imagePath, String url) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.dateTime = dateTime;
        this.color = color;
        this.imagePath = imagePath;
        this.url = url;
    }

    private ArrayList<AbstractItem> mData = new ArrayList<>();

    public void setData(ArrayList<AbstractItem> newData) {
        mData.clear();
        mData.addAll(newData);
        notify();
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public Object withIsDraggable(boolean draggable) {
        return null;
    }

    public interface OnBtnDetailClickListener{
        void onBtnDetailsClicked(int itemId);
    }

    private OnBtnDetailClickListener mBtnDetailClickListener;

    public void setBtnDetailClickListener(OnBtnDetailClickListener btnDetailClickListener) {
        mBtnDetailClickListener = btnDetailClickListener;
    }

    public OnBtnDetailClickListener getBtnDetailClickListener() {
        return mBtnDetailClickListener;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @NonNull
    @Override
    public CreateNotesAdapter.ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_container_note;
    }



    class ViewHolder extends FastAdapter.ViewHolder {

        TextView title,subTItle,date;
        LinearLayout layoutNote;
        RoundedImageView imagesNote;
        ImageView selectedImg, optionIcon, favIconShowSelected, lockIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textTitle);
            subTItle = itemView.findViewById(R.id.textSubtitles);
            date = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imagesNote = itemView.findViewById(R.id.imagesNote);
            selectedImg = itemView.findViewById(R.id.overImg);
            optionIcon = itemView.findViewById(R.id.optionIconsBtn);
            favIconShowSelected = itemView.findViewById(R.id.favSet);
            lockIcon = itemView.findViewById(R.id.LockIcon);

        }


        @Override
        public void unbindView(IItem item) {

        }

        @Override
        public void bindView(IItem item, List payloads) {
            if (item instanceof CreateNotesAdapter) {
                CreateNotesAdapter createNotesAdapter = (CreateNotesAdapter) item;

                title.setText(createNotesAdapter.getTitle());
                subTItle.setText(createNotesAdapter.getSubTitle());
                date.setText(createNotesAdapter.getDateTime());

                GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
                if (createNotesAdapter.getColor() != null){
                    gradientDrawable.setColor(Color.parseColor(createNotesAdapter.getColor()));
                } else {
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                }

                if (createNotesAdapter.getImagePath() != null){
                    imagesNote.setImageBitmap(BitmapFactory.decodeFile(createNotesAdapter.getImagePath()));
                    imagesNote.setVisibility(View.VISIBLE);
                } else {
                    imagesNote.setVisibility(View.GONE);
                }

                if (createNotesAdapter.isSelected()) {
                    selectedImg.setVisibility(View.VISIBLE);
                } else {
                    selectedImg.setVisibility(View.GONE);
                }

                if (createNotesAdapter.isFavorite()) {
                    favIconShowSelected.setVisibility(View.VISIBLE);
                } else {
                    favIconShowSelected.setVisibility(View.GONE);
                }

                if (createNotesAdapter.isNoteLock()){
                    lockIcon.setVisibility(View.VISIBLE);
                } else{
                    lockIcon.setVisibility(View.GONE);
                }

                optionIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mBtnDetailClickListener != null) {
                            mBtnDetailClickListener.onBtnDetailsClicked(createNotesAdapter.getId());
                        }
                    }
                });

            } else if (item instanceof AddLabelAdapter) {
                AddLabelAdapter addLabelAdapter = (AddLabelAdapter) item;

                // Set label-specific views here
                title.setText(addLabelAdapter.getAddLabel());
                // Set other label views as needed

                // Hide any views that are specific to notes (if needed)
                imagesNote.setVisibility(View.GONE);
                selectedImg.setVisibility(View.GONE);
                favIconShowSelected.setVisibility(View.GONE);
                lockIcon.setVisibility(View.GONE);
                optionIcon.setOnClickListener(null);
            }
        }

    }

}
