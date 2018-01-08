package org.secuso.privacyfriendlynotes;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ViewTextNoteActivity extends ViewNoteActivity {

    private String content;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_text_note);
        super.initViewNoteActivity();

        TextView content = (TextView) findViewById(R.id.note_content);
//        TextView name = (TextView) findViewById(R.id.note_name);

        Cursor cursor = DbAccess.getNote(getBaseContext(), noteID);
        cursor.moveToFirst();

//        this.name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
        this.content = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT));

        content.setText(this.content);
//        name.setText(this.name);
    }

    @Override
    protected Intent getShareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, this.name + "\n\n" + this.content);
        return sendIntent;
    }

    @Override
    protected void startEditActivity() {
        Intent i = new Intent(getApplication(), TextNoteActivity.class);
        i.putExtra(TextNoteActivity.EXTRA_ID, noteID);
        startActivity(i);
    }

    @Override
    protected void saveToExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File path;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                path = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "/PrivacyFriendlyNotes");
            } else{
                path = new File(Environment.getExternalStorageDirectory(), "/PrivacyFriendlyNotes");
            }

            File file = new File(path, "/text_" + this.name + ".txt");
            try {
                // Make sure the directory exists.
                boolean path_exists = path.exists() || path.mkdirs();
                if (path_exists) {

                    PrintWriter out = new PrintWriter(file);
                    out.println(this.name);
                    out.println();
                    out.println(this.name);
                    out.close();
                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
                    MediaScannerConnection.scanFile(this,
                            new String[] { file.toString() }, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });

                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.toast_file_exported_to), file.getAbsolutePath()), Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                // Unable to create file, likely because external storage is
                // not currently mounted.
                Log.w("ExternalStorage", "Error writing " + file, e);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_external_storage_not_mounted, Toast.LENGTH_LONG).show();
        }
    }
}
