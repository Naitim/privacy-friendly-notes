package org.secuso.privacyfriendlynotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public abstract class ViewNoteActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, View.OnClickListener{
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";

    protected FloatingActionButton fab;
    protected Toolbar toolbar;
    protected ShareActionProvider mShareActionProvider;


    protected int noteID;

    // It's important that this is called in the subclasses' onCreate method
    protected void initViewNoteActivity(){
        fab = (FloatingActionButton) findViewById(R.id.edit_fab);
        fab.setOnClickListener(this);

        noteID = getIntent().getIntExtra(EXTRA_ID, -1);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(DbAccess.getName(getBaseContext(), noteID));

        //        setSupportActionBar(toolbar);

        // show reminders

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        System.out.println("onclick");
        switch (item.getItemId()) {
            case R.id.edit_fab:
                startEditActivity();
                return true;
            case R.id.btn_delete:
                displayTrashDialog();
                return true;
            case R.id.btn_save:
                saveToExternalStorage();
                return true;
            default:
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.edit_fab:
                startEditActivity();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_note_menu, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(getShareIntent());
        return true;
    }

    private void displayTrashDialog() {
        System.out.println("displaytrash");
        SharedPreferences sp = getSharedPreferences(Preferences.SP_DATA, Context.MODE_PRIVATE);
        if (sp.getBoolean(Preferences.SP_DATA_DISPLAY_TRASH_MESSAGE, true)){
            //we never displayed the message before, so show it now
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_trash_title))
                    .setMessage(getString(R.string.dialog_trash_message))
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DbAccess.trashNote(getBaseContext(), noteID);
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(Preferences.SP_DATA_DISPLAY_TRASH_MESSAGE, false);
            editor.commit();
        } else {
            DbAccess.trashNote(getBaseContext(), noteID);
            finish();
        }
    }

    protected abstract Intent getShareIntent();

    protected abstract void startEditActivity();

    protected abstract void saveToExternalStorage();
}
