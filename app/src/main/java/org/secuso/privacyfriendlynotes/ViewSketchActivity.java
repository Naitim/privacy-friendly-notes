package org.secuso.privacyfriendlynotes;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ViewSketchActivity  extends ViewNoteActivity{

    private Image image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sketch);

        super.initViewNoteActivity();

        ImageView image = findViewById(R.id.note_content);

        Cursor cursor = DbAccess.getNote(getBaseContext(), noteID);
        cursor.moveToFirst();

        String path = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT));
        //TODO: Load image
    }

    @Override
    protected Intent getShareIntent() {
        //TODO
        return null;
    }

    //taken from http://stackoverflow.com/a/10616868
    private static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }

    @Override
    protected void startEditActivity() {

    }

    @Override
    protected void saveToExternalStorage() {

    }
}
