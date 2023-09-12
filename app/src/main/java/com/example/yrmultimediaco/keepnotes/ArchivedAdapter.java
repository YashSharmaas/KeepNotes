package com.example.yrmultimediaco.keepnotes;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.makeramen.roundedimageview.RoundedImageView;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class ArchivedAdapter extends AbstractItem<ArchivedAdapter, ArchivedAdapter.ViewHolder> {

    int id;
    String title, subTitle, description, dateTime, imagePath;
    String color;
    ImageView archivedImage;
    private ButtonClickListner mButtonClickListner;

    public ArchivedAdapter(int id, String title, String subTitle, String description, String color ,String dateTime, String imagePath) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.color = color;
        this.dateTime = dateTime;
        this.imagePath = imagePath;

    }

    public interface ButtonClickListner{

        void onArchivedButtonClick(int noteId);

    }

    public void setButtonClickListner(ButtonClickListner buttonClickListner) {
        mButtonClickListner = buttonClickListner;
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

    public ImageView getArchivedImage() {
        return archivedImage;
    }

    public void setArchivedImage(ImageView archivedImage) {
        this.archivedImage = archivedImage;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.unarchived_note_item_container;
    }

class ViewHolder extends FastAdapter.ViewHolder {

    TextView title, subTitle, date;
    LinearLayout layoutNote;
    RoundedImageView imagesNote;
    ImageView archivedImage;

    public ViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.textTitle);
        subTitle = itemView.findViewById(R.id.textSubtitles);
        date = itemView.findViewById(R.id.textDateTime);
        layoutNote = itemView.findViewById(R.id.layoutNote);
        imagesNote = itemView.findViewById(R.id.imagesNote);
        archivedImage = itemView.findViewById(R.id.unarchiveBtn);

    }

    @Override
    public void unbindView(IItem item) {

    }

    @Override
    public void bindView(IItem item, List payloads) {

        ArchivedAdapter adapter = (ArchivedAdapter) item;

        title.setText(adapter.getTitle());
        subTitle.setText(adapter.getSubTitle());
        date.setText(adapter.getDateTime());

        GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
        if (adapter.getColor() != null) {
            gradientDrawable.setColor(Color.parseColor(adapter.getColor()));
        } else {
            gradientDrawable.setColor(Color.parseColor("#333333"));
        }

        if (adapter.getImagePath() != null) {
            imagesNote.setImageBitmap(BitmapFactory.decodeFile(adapter.getImagePath()));
            imagesNote.setVisibility(View.VISIBLE);
        } else {
            imagesNote.setVisibility(View.GONE);
        }

        archivedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mButtonClickListner != null){
                    mButtonClickListner.onArchivedButtonClick(adapter.getId());
                }
            }
        });

    }

}

}
