package org.secuso.privacyfriendlynotes.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.viewholders.FlexibleViewHolder;
import org.secuso.privacyfriendlynotes.DbAccess;
import org.secuso.privacyfriendlynotes.DbContract;
import org.secuso.privacyfriendlynotes.R;

import java.util.List;

public class NoteItem extends AbstractFlexibleItem<NoteItem.ViewHolder> implements IFilterable {

    private int id;
    private String title;
    private String content;
    private String bottom_text;
    private Drawable icon;
    private Drawable trashIcon;
    private Cursor cursor;
    // TODO: Maybe use Android O Color
    private int color;


    public NoteItem(Cursor c, Activity activity) {

        this.id = c.getInt((c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
        this.title = c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
        this.color = c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_COLOR));
        this.trashIcon = activity.getResources().getDrawable(R.drawable.ic_delete_black_24dp);
        this.cursor = c;

        int cat = c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CATEGORY));
        Cursor catCursor = DbAccess.getCategory(activity.getBaseContext(), cat);
        catCursor.moveToFirst();
        this.bottom_text = activity.getString(R.string.category) + ": " + catCursor.getString(catCursor.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_NAME));

        switch (c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
            case DbContract.NoteEntry.TYPE_SKETCH:
                this.icon = activity.getResources().getDrawable(R.drawable.ic_photo_black_24dp, activity.getTheme());
                break;
            case DbContract.NoteEntry.TYPE_AUDIO:
                this.icon = activity.getResources().getDrawable(R.drawable.ic_mic_black_24dp, activity.getTheme());
                break;
            case DbContract.NoteEntry.TYPE_TEXT:
                this.icon = activity.getResources().getDrawable(R.drawable.ic_short_text_black_24dp, activity.getTheme());
                this.content = c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT));
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

        if (content == null || content.equals(""))
            holder.mText.setHeight(0);

        if (title != null) {
            holder.mTitle.setText(title);
            holder.mTitle.setEnabled(isEnabled());
        }
        if (content != null && !content.equals("")) {
            holder.mText.setText(content);
            holder.mText.setMaxLines(3);
            holder.mText.setEnabled(isEnabled());
        }
        if (icon != null) {
            holder.mIcon.setImageDrawable(icon);
        }
        holder.mTrash.setImageDrawable(trashIcon);

        holder.bottom_bar.setText(bottom_text);

//        System.out.println("color of " + title + ": " + color);

//        holder.mCard.setCardBackgroundColor(color);

        // DrawableUtils for Animations
        int pressedColor = highlightColor(color, 0.8f);


        Context context = holder.itemView.getContext();
        Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
                color, pressedColor, // Same color of divider
                DrawableUtils.getColorControlHighlight(context));
        DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
    }

    // helper for getting highlight-color
    private static int highlightColor(int color, float factor) {
        int a = Color.alpha( color );
        int r = Color.red( color );
        int g = Color.green( color );
        int b = Color.blue( color );

        return Color.argb( a,
                Math.max( (int)(r * factor), 0 ),
                Math.max( (int)(g * factor), 0 ),
                Math.max( (int)(b * factor), 0 ) );
    }

    @Override
    public boolean filter(String constraint) {
        if (content != null && content.toLowerCase().contains(constraint))
            return  true;

        if(title != null && title.toLowerCase().contains(constraint))
            return true;

        return false;
    }

    public static class ViewHolder extends FlexibleViewHolder {

        public TextView mTitle;
        public TextView mText;
        public ImageView mIcon;
        public ImageView mTrash;
        public CardView mCard;
        public RelativeLayout top_bar;
        public TextView bottom_bar;

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
