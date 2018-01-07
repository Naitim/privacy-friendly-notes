package org.secuso.privacyfriendlynotes;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.*;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.secuso.privacyfriendlynotes.fragments.NotesListFragment;
import org.secuso.privacyfriendlynotes.fragments.WelcomeDialog;
import org.secuso.privacyfriendlynotes.util.NoteItem;

import java.util.Comparator;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, android.support.v7.widget.SearchView.OnQueryTextListener {

    private static final int CAT_ALL = -1;
    private static final String TAG_WELCOME_DIALOG = "welcome_dialog";
    FloatingActionsMenu fabMenu;

    private SearchView mSearchView;

    private ActionMode mActionMode;

    private NotesListFragment notesListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set the OnClickListeners
        findViewById(R.id.fab_text).setOnClickListener(this);
        findViewById(R.id.fab_checklist).setOnClickListener(this);
        findViewById(R.id.fab_audio).setOnClickListener(this);
        findViewById(R.id.fab_sketch).setOnClickListener(this);

        notesListFragment = (NotesListFragment) getFragmentManager().findFragmentById(R.id.list_notes);

        fabMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        SharedPreferences sp = getSharedPreferences(Preferences.SP_DATA, Context.MODE_PRIVATE);
        if (sp.getBoolean(Preferences.SP_DATA_DISPLAY_WELCOME_DIALOG, true)) {
            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.show(getFragmentManager(), TAG_WELCOME_DIALOG);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(Preferences.SP_DATA_DISPLAY_WELCOME_DIALOG, false);
            editor.commit();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        buildDrawerMenu();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setOnActionExpandListener(
                    new MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            notesListFragment.startSearch();
                            fabMenu.setVisibility(View.INVISIBLE);
//                            MenuItem listTypeItem = menu.findItem(R.id.action_list_type);
//                            if (listTypeItem != null)
//                                listTypeItem.setVisible(false);
                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            fabMenu.setVisibility(View.VISIBLE);
                            notesListFragment.stopSearch();
//                            MenuItem listTypeItem = menu.findItem(R.id.action_list_type);
//                            if (listTypeItem != null)
//                                listTypeItem.setVisible(true);
                            return true;
                        }
                    });
            mSearchView = (SearchView) searchItem.getActionView();
            mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
            mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN);
            mSearchView.setQueryHint(getString(R.string.search_hint));
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            mSearchView.setOnQueryTextListener(this);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_alphabetical) {
            //switch to an alphabetically sorted cursor.
            notesListFragment.sortItems(new Comparator<NoteItem>() {
                @Override
                public int compare(NoteItem a, NoteItem b) {
                    return a.getTitle().compareToIgnoreCase(b.getTitle());
                }
            });
            return true;
        } else if (id == R.id.action_help) {
            startActivity(new Intent(getApplication(), HelpActivity.class));
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(getApplication(), SettingsActivity.class));
        } else if (id == R.id.action_about) {
            startActivity(new Intent(getApplication(), AboutActivity.class));
        } else if (id == R.id.action_search){
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        item.setCheckable(true);
        item.setChecked(true);
        int id = item.getItemId();
        if (id == R.id.nav_trash) {
            startActivity(new Intent(getApplication(), RecycleActivity.class));
        } else if (id == R.id.nav_all) {
            notesListFragment.setSelectedCategory(CAT_ALL);
        } else if (id == R.id.nav_manage_categories) {
            startActivity(new Intent(getApplication(), ManageCategoriesActivity.class));
        } else {
            notesListFragment.setSelectedCategory(id);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_text:
                startActivity(new Intent(getApplication(), TextNoteActivity.class));
                fabMenu.collapseImmediately();
                break;
            case R.id.fab_checklist:
                startActivity(new Intent(getApplication(), ChecklistNoteActivity.class));
                fabMenu.collapseImmediately();
                break;
            case R.id.fab_audio:
                startActivity(new Intent(getApplication(), AudioNoteActivity.class));
                fabMenu.collapseImmediately();
                break;
            case R.id.fab_sketch:
                startActivity(new Intent(getApplication(), SketchActivity.class));
                fabMenu.collapseImmediately();
                break;
        }
    }

    private void buildDrawerMenu() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navMenu = navigationView.getMenu();
        //reset the menu
        navMenu.clear();
        //Inflate the standard stuff
        MenuInflater menuInflater = new MenuInflater(getApplicationContext());
        menuInflater.inflate(R.menu.activity_main_drawer, navMenu);
        //Get the rest from the database
        Cursor c = DbAccess.getCategories(getBaseContext());
        while (c.moveToNext()){
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_NAME));
            int id = c.getInt(c.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_ID));
            navMenu.add(R.id.drawer_group2, id, Menu.NONE, name).setIcon(R.drawable.ic_label_black_24dp);
        }
        c.close();
    }

    private void deleteSelectedItems(){
        ListView notesList = (ListView) findViewById(R.id.notes_list);
        CursorAdapter adapter = (CursorAdapter) notesList.getAdapter();
        SparseBooleanArray checkedItemPositions = notesList.getCheckedItemPositions();
        for (int i=0; i < checkedItemPositions.size(); i++) {
            if(checkedItemPositions.valueAt(i)) {
                DbAccess.trashNote(getBaseContext(), (int) (long) adapter.getItemId(checkedItemPositions.keyAt(i)));
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        notesListFragment.filter(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        notesListFragment.filter(s);
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            notesListFragment.filter(query);
        }

    }
}
