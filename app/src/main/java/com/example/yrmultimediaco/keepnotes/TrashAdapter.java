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

public class TrashAdapter extends AbstractItem<TrashAdapter, TrashAdapter.ViewHolder> {

    int id;
    String title, subTitle, description, dateTime, imagePath;
    String color;
    ImageView restoreImage, deleteImage;
    private ButtonClickListner mButtonClickListner;

    public TrashAdapter(int id, String title, String subTitle, String description, String color ,String dateTime, String imagePath/*, ButtonClickListner buttonClickListner*/) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.color = color;
        this.dateTime = dateTime;
        this.imagePath = imagePath;
//        mButtonClickListner = buttonClickListner;
    }

    public interface ButtonClickListner{
        void onRestoreButtonClick(int noteId);
        void onDeleteButtonClick(int noteId);
    }

    public void setButtonClickListner(ButtonClickListner buttonClickListner) {
        mButtonClickListner = buttonClickListner;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ImageView getRestoreImage() {
        return restoreImage;
    }

    public void setRestoreImage(ImageView restoreImage) {
        this.restoreImage = restoreImage;
    }

    public ImageView getDeleteImage() {
        return deleteImage;
    }

    public void setDeleteImage(ImageView deleteImage) {
        this.deleteImage = deleteImage;
    }


    @NonNull
    @Override
    public TrashAdapter.ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.trash_note_item_container;
    }

   class ViewHolder extends FastAdapter.ViewHolder {

       TextView title,subTItle,date;
       LinearLayout layoutNote;
       RoundedImageView imagesNote;
       ImageView restoreImage, deleteImage;

       public ViewHolder(View itemView) {
           super(itemView);

           title = itemView.findViewById(R.id.textTitle);
           subTItle = itemView.findViewById(R.id.textSubtitles);
           date = itemView.findViewById(R.id.textDateTime);
           layoutNote = itemView.findViewById(R.id.layoutNote);
           imagesNote = itemView.findViewById(R.id.imagesNote);
           restoreImage = itemView.findViewById(R.id.restoreIconsBtn);
           deleteImage = itemView.findViewById(R.id.deleteBtn);

       }

       @Override
       public void unbindView(IItem item) {

       }

       @Override
       public void bindView(IItem item, List payloads) {

        TrashAdapter trashAdapter = (TrashAdapter) item;

           title.setText(trashAdapter.getTitle());
           subTItle.setText(trashAdapter.getSubTitle());
           date.setText(trashAdapter.getDateTime());

           GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
           if (trashAdapter.getColor() != null){
               gradientDrawable.setColor(Color.parseColor(trashAdapter.getColor()));
           } else {
               gradientDrawable.setColor(Color.parseColor("#333333"));
           }

           if (trashAdapter.getImagePath() != null){
               imagesNote.setImageBitmap(BitmapFactory.decodeFile(trashAdapter.getImagePath()));
               imagesNote.setVisibility(View.VISIBLE);
           } else {
               imagesNote.setVisibility(View.GONE);
           }

           restoreImage.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   if (mButtonClickListner != null) {
                       mButtonClickListner.onRestoreButtonClick(trashAdapter.getId());
                   }
               }
           });

           deleteImage.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   if (mButtonClickListner != null){
                       mButtonClickListner.onDeleteButtonClick(trashAdapter.getId());
                   }
               }
           });

       }
   }

}
