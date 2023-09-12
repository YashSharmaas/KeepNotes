/*
package com.example.yrmultimediaco.keepnotes;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.makeramen.roundedimageview.RoundedImageView;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class AddCategoryAdapter extends AbstractItem<AddCategoryAdapter, AddCategoryAdapter.ViewHoler> {

    int id;
    String title, subTitle, description, dateTime, imagePath;
    String color;

    public AddCategoryAdapter(int id, String title, String subTitle, String description, String dateTime, String imagePath, String color) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.dateTime = dateTime;
        this.imagePath = imagePath;
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

    @NonNull
    @Override
    public ViewHoler getViewHolder(View v) {
        return new ViewHoler(v);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_category_container;
    }

    public class ViewHoler extends FastAdapter.ViewHolder {

        TextView title,subTItle,date;
        LinearLayout layoutNote;
        RoundedImageView imagesNote;

        public ViewHoler(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textTitle);
            subTItle = itemView.findViewById(R.id.textSubtitles);
            date = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imagesNote = itemView.findViewById(R.id.imagesNote);
        }

        @Override
        public void unbindView(IItem item) {

        }

        @Override
        public void bindView(IItem item, List payloads) {

            AddCategoryAdapter addCategoryAdapter = (AddCategoryAdapter) item;

            title.setText(addCategoryAdapter.getTitle());
            subTItle.setText(addCategoryAdapter.getSubTitle());
            date.setText(addCategoryAdapter.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (addCategoryAdapter.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(addCategoryAdapter.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (addCategoryAdapter.getImagePath() != null){
                imagesNote.setImageBitmap(BitmapFactory.decodeFile(addCategoryAdapter.getImagePath()));
                imagesNote.setVisibility(View.VISIBLE);
            } else {
                imagesNote.setVisibility(View.GONE);
            }

        }
    }



}

*/
