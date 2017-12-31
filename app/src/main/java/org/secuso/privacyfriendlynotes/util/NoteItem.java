package org.secuso.privacyfriendlynotes.util;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import org.secuso.privacyfriendlynotes.DbContract;
import org.secuso.privacyfriendlynotes.R;

import java.util.List;

public class NoteItem extends AbstractFlexibleItem<NoteItem.ViewHolder> {

    private int id;
    private String title;
    private String content;
    private Drawable icon;
    private Drawable trashIcon;
    //
    private Cursor cursor;
    //Maybe use Android O Color
    private int color;


    public NoteItem(Cursor c, Activity activity){

        this.id = c.getInt((c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
        this.title = c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
        this.color = c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_COLOR));
        this.trashIcon = activity.getResources().getDrawable(R.drawable.ic_delete_black_24dp);
        this.cursor = c;

        switch (c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
            case DbContract.NoteEntry.TYPE_SKETCH:
                this.icon = activity.getResources().getDrawable(R.drawable.ic_photo_black_24dp, activity.getTheme());
                break;
            case DbContract.NoteEntry.TYPE_AUDIO:
                this.icon = activity.getResources().getDrawable(R.drawable.ic_mic_black_24dp, activity.getTheme());
                break;
            case DbContract.NoteEntry.TYPE_TEXT:
                this.icon = activity.getResources().getDrawable(R.drawable.ic_short_text_black_24dp, activity.getTheme());
                this.content = truncateNoteText(c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT)));
                break;
            case DbContract.NoteEntry.TYPE_CHECKLIST:
                this.icon = activity.getResources().getDrawable(R.drawable.ic_format_list_bulleted_black_24dp, activity.getTheme());
                break;
            default:
        }

        setSelectable(true);
        //Allow dragging
        setDraggable(true);
    }

    // TODO: use maxlines in the textview instead
    private String truncateNoteText(String text) {
        //TODO: implement this properly
        //TODO: Maybe also truncate the note title
        int tmp = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == "\n".charAt(0)) {
                tmp++;
                if (tmp > 1) {
                    return text.substring(0, i);
                }
            }
        }
        if(text.length() > 50)
            return text.substring(0, 50);
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteItem that = (NoteItem) o;
        return id == that.id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_note;
    }

    @Override
    public ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new NoteItem.ViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {

        if(content == null || content.equals(""))
            holder.mText.setHeight(0);

        if (title != null) {
            holder.mTitle.setText(title);
            holder.mTitle.setEnabled(isEnabled());
        }
        if (content != null && !content.equals("")) {
            holder.mText.setText(content);
            holder.mText.setEnabled(isEnabled());
        }
        if (icon != null) {
            holder.mIcon.setImageDrawable(icon);
        }
        holder.mTrash.setImageDrawable(trashIcon);

//        System.out.println("color of " + title + ": " + color);

        holder.mCard.setCardBackgroundColor(color);

        //TODO: doesn't work, just don't make black available and create a new color palette for readable note colors
            //compensation for dark colors
//
//            int red = (color >>> 16) & 0x000000FF;
//            int green = (color >>> 8) & 0x000000FF;
//            int blue = (color) & 0x000000FF;
//
//            System.out.println("red: " + red);
//
//            System.out.println("color average: " + (red + green + blue) / 3);
//            //simple check to see how dark the color is
//            if((red + green + blue) / 3 < 60){
////                holder.top_bar.setBackgroundTintList(ColorStateList.valueOf(R.color.white_ultra_transparent));
////                holder.top_bar.setBackgroundColor(0x12FFFFFF);
//                holder.top_bar.setBackgroundResource(R.color.white_ultra_transparent);
////                holder.bottom_bar.setBackgroundColor(R.color.white_ultra_transparent);
//            }
//        }
    }

    public static class ViewHolder extends FlexibleViewHolder{

        public TextView mTitle;
        public TextView mText;
        public ImageView mIcon;
        public ImageView mTrash;
        public CardView mCard;
        public RelativeLayout top_bar;
        public TextView bottom_bar;

        @Override
        public void toggleActivation() {
            super.toggleActivation();
            System.out.println("toggleActivation");
        }

        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            mTitle = view.findViewById(R.id.note_title);
            mText = view.findViewById(R.id.note_text);
            mIcon = view.findViewById(R.id.item_icon);
            mCard = view.findViewById(R.id.note_card);
            mTrash = view.findViewById(R.id.image_trash);

            top_bar = view.findViewById(R.id.top_bar);
            bottom_bar = view.findViewById(R.id.bottom_bar);

            //top_bar is a bit annoying with scrolling
//            setDragHandleView(top_bar);
            setDragHandleView(mIcon);
        }

        //        @Override
//        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
//            if (mAdapter.getRecyclerView().getLayoutManager() instanceof GridLayoutManager) {
//                if (position % 2 != 0)
//                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
//                else
//                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
//            } else {
//                if (isForward)
//                    AnimatorHelper.slideInFromBottomAnimator(animators, itemView, mAdapter.getRecyclerView());
//                else
//                    AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.getRecyclerView());
//            }
//        }
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public Cursor getCursor() {
        return cursor;
    }
}
