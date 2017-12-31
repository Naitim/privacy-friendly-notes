package org.secuso.privacyfriendlynotes.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.*;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.Payload;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;
import eu.davidea.flexibleadapter.helpers.UndoHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.secuso.privacyfriendlynotes.*;
import org.secuso.privacyfriendlynotes.util.NoteItem;
import org.secuso.privacyfriendlynotes.util.OrderedAdapter;

import java.io.Serializable;
import java.util.*;

public class NotesListFragment extends Fragment implements AppCompatCallback, UndoHelper.OnUndoListener, ActionMode.Callback, FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemLongClickListener{

    private OrderedAdapter<NoteItem> noteAdapter;

    private ActionModeHelper actionModeHelper;
    private RecyclerView rv;

    private RecyclerView.AdapterDataObserver observer;

    private static final int CAT_ALL = -1;
    private int selectedCategory = CAT_ALL; //ID of the currently selected category. Defaults to "all"

    private Map<Integer, List<Integer>> orderIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        orderIds = new HashMap<>();

        //Fetch last ordering of notes
        JSONObject map = new JSONObject(new HashMap<String, JSONArray>());
        try {
            map = new JSONObject(getActivity().getPreferences(Context.MODE_PRIVATE).getString("ordering", map.toString()));

            for (Iterator<String> it = map.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONArray array = (JSONArray) map.get(key);
                List<Integer> list = new LinkedList<>();
                for(int i = 0; i < array.length(); i++){
                    list.add((Integer) array.get(i));
                }
                orderIds.put(Integer.valueOf(key), list);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notes_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        rv = view.findViewById(R.id.rv);

        GridLayoutManager lm = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(lm);

        List<NoteItem> myItems = restoreOrdering(getDatabaseList(selectedCategory), selectedCategory);
        noteAdapter = new OrderedAdapter(myItems);
        noteAdapter.addListener(this);

        rv.setAdapter(noteAdapter);

        observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateOrdering();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                System.out.println(actionModeHelper.getActionMode());
                updateOrdering();
            }
        };

        noteAdapter.registerAdapterDataObserver(observer);

//        noteAdapter.setLongPressDragEnabled(true);
        noteAdapter.setHandleDragEnabled(true);
        noteAdapter.setMode(Mode.IDLE);
//        actionModeHelper.withDefaultMode(Mode.MULTI);
        initializeActionModeHelper(Mode.MULTI);
    }

    public void updateOrdering(){
        List idList = new LinkedList<>();
        for (NoteItem note : noteAdapter.getCurrentItems()) {
            idList.add(note.getId());
        }
        orderIds.put(selectedCategory, idList);
    }

    private List<NoteItem> getDatabaseList(int category) {
        List<NoteItem> list = new ArrayList();

        Cursor c;
        if(category == -1){ //show all
            String selection = DbContract.NoteEntry.COLUMN_TRASH + " = ?";
            String[] selectionArgs = {"0"};
            c = DbAccess.getCursorAllNotes(getActivity().getBaseContext(), selection, selectionArgs);
        } else {
            String selection = DbContract.NoteEntry.COLUMN_CATEGORY + " = ? AND " + DbContract.NoteEntry.COLUMN_TRASH + " = ?";
            String[] selectionArgs = { String.valueOf(category), "0" };
            c = DbAccess.getCursorAllNotes(getActivity().getBaseContext(), selection, selectionArgs);
        }

        for(int i = 0; i < c.getCount(); i++){
            c.moveToPosition(i);
            list.add(new NoteItem(c, getActivity()));
        }


        return list;
    }

    private List<NoteItem> restoreOrdering(List<NoteItem> notes, int category){
        if(orderIds == null || orderIds.get(category) == null){
            return notes;
        }
        List<NoteItem> tmp = new LinkedList<>();
        for(int i = 0; i < orderIds.get(category).size(); i++) {
            for (NoteItem note : notes) {
                if (note.getId() == orderIds.get(category).get(i)) {
                    tmp.add(note);
                    break;
                }
            }
        }
        //add all remaining elements just in case there are new notes
        notes.removeAll(tmp);
        tmp.addAll(notes);

        return tmp;
    }

    public void updateList(){
        //TODO: Maybe do some comparing of lists and manually add/remove items instead of updateDataSet, gives some nice animations (maybe overwrite updateDataSet?)
        noteAdapter.updateDataSet(restoreOrdering(getDatabaseList(selectedCategory), selectedCategory), false);
    }

    public void sortItems(Comparator<NoteItem> comp){
        noteAdapter.sortItems(comp);
    }

    public void setSelectedCategory(int category) {
        this.selectedCategory = category;
        changeCategory(category);
    }

    public void changeCategory(int category){
        //TODO In this function, fetch the list of items of the selected category and update the adapter
        // Make it animated by not updating the dataset but instead comparing the lists and using addItem, removeItem, and reArrangeItems
        // this function becomes unnecessary if updateList implements animation in the same way
        updateList();
    }


    @Override
    public void onStop() {
        super.onStop();

        JSONObject map = new JSONObject(new HashMap<String, JSONArray>());
        for(int key : orderIds.keySet()){
            JSONArray array = new JSONArray(orderIds.get(key));
            try {
                map.put(Integer.toString(key), array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        getActivity().getPreferences(Context.MODE_PRIVATE).edit().putString("ordering", map.toString()).apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putSerializable("orderIds", (Serializable) orderIds);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            orderIds = (Map<Integer, List<Integer>>) savedInstanceState.getSerializable("orderIds");
        }

        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.main_cab, menu);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.black_semi_transparent, getActivity().getTheme()));
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        System.out.println("SOMETHING???");
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        System.out.println("actionmode something");
        switch (item.getItemId()) {
            case R.id.action_delete:
                // Build message before delete, for the SnackBar
                StringBuilder message = new StringBuilder();
                message.append("getString(R.string.action_deleted)").append(" ");
                for (Integer pos : noteAdapter.getSelectedPositions()) {
                    message.append("extractTitleFrom(mAdapter.getItem(pos))");
                    if (noteAdapter.getSelectedItemCount() > 1)
                        message.append(", ");
                }

                // Experimenting NEW feature
                noteAdapter.setRestoreSelectionOnUndo(true);
                noteAdapter.setPermanentDelete(false);

                // New Undo Helper (Basic usage)
                new UndoHelper((FlexibleAdapter) noteAdapter, this)
                        .withPayload(Payload.CHANGE)
                        .start(noteAdapter.getSelectedPositions(),
                                getView().findViewById(R.id.main_content), message,
                                "getString(R.string.undo)", UndoHelper.UNDO_TIMEOUT);

                // Enable Refreshing
//                mRefreshHandler.sendEmptyMessage(REFRESH_START);
//                mRefreshHandler.sendEmptyMessageDelayed(REFRESH_STOP, UndoHelper.UNDO_TIMEOUT);

                // Finish the action mode
                actionModeHelper.destroyActionModeIfCan();

                // We consume the event
                return true;

            default:
                // If an item is not implemented we don't consume the event, so we finish the ActionMode
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.white, getActivity().getTheme()));
    }

    @Override
    public boolean onItemClick(int position) {
        System.out.println("SOMETHING???!");
        // Action on elements are allowed if Mode is IDLE, otherwise selection has priority
        if (noteAdapter.getMode() != Mode.IDLE && actionModeHelper != null) {
            boolean activate = actionModeHelper.onClick(position);
            // Last activated position is now available
//            Log.d(TAG, "Last activated position " + actionModeHelper.getActivatedPosition());
            System.out.println("Last activated position " + actionModeHelper.getActivatedPosition());
            return activate;
        } else {
            // Handle the item click listener
            System.out.println("Handle item click listener");
            clickItem(position);
            return false;
        }
    }

    private void clickItem(int position){
        NoteItem item = noteAdapter.getItem(position);
        Cursor c = item.getCursor();
        //start the appropriate activity
        switch (c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
            case DbContract.NoteEntry.TYPE_TEXT:
                Intent i = new Intent(getActivity().getApplication(), TextNoteActivity.class);
                i.putExtra(TextNoteActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
                startActivity(i);
                break;
            case DbContract.NoteEntry.TYPE_AUDIO:
                Intent i2 = new Intent(getActivity().getApplication(), AudioNoteActivity.class);
                i2.putExtra(AudioNoteActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
                startActivity(i2);
                break;
            case DbContract.NoteEntry.TYPE_SKETCH:
                Intent i3 = new Intent(getActivity().getApplication(), SketchActivity.class);
                i3.putExtra(SketchActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
                startActivity(i3);
                break;
            case DbContract.NoteEntry.TYPE_CHECKLIST:
                Intent i4 = new Intent(getActivity().getApplication(), ChecklistNoteActivity.class);
                i4.putExtra(ChecklistNoteActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
                startActivity(i4);
                break;
        }
    }

    @Override
    public void onItemLongClick(int position) {
        System.out.println("SOMETHING???");
        actionModeHelper.onLongClick((AppCompatActivity) getActivity(), position);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        System.out.println("actionmode something");
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        System.out.println("actionmode something");
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        System.out.println("actionmode something");
        return null;
    }


    private void initializeActionModeHelper(@SelectableAdapter.Mode int mode) {
        System.out.println("initializeActionModeHelper \n\n");
        //this = ActionMode.Callback instance
        actionModeHelper = new ActionModeHelper(noteAdapter, R.menu.main_cab, this) {
            // Override to customize the title
            @Override
            public void updateContextTitle(int count) {
                // You can use the internal mActionMode instance
                if (mActionMode != null) {
                    mActionMode.setTitle(count == 1 ?
                            "Selected one" :
                            "Selected many");
                }
            }
        }.withDefaultMode(mode);
    }


    // undohelper
    @Override
    public void onActionCanceled(int action) {
        System.out.println("Delete Canceled");
    }

    @Override
    public void onActionConfirmed(int action, int event) {
        System.out.println("Delete Confirmed");
    }


//    /**
//     * Update the list and restore the last known ordering
//     */
//    public void updateList(boolean animated){
//        updateList(SortMode.Remembered, animated);
//    }
//


//
//    public void reArrangeItems(List<NoteItem> resultList){
//        List<NoteItem> current = noteAdapter.getCurrentItems();
//
//        for(int i = 0; i < resultList.size(); i++){
//            int tmp = noteAdapter.getGlobalPositionOf(resultList.get(i));
//            noteAdapter.moveItem(tmp, i);
//        }
//    }

//    public void updateList(SortMode order, boolean animated){
//        List<NoteItem> notes = getDatabaseList();
//
//        Comparator<NoteItem> comp;
//
//        switch (order){
//            case Unordered:
//                break;
//            case Alphabetical:
//                comp = new Comparator<NoteItem>() {
//                    @Override
//                    public int compare(NoteItem a, NoteItem b) {
//                        return a.getTitle().compareToIgnoreCase(b.getTitle());
//                    }
//                };
//                Collections.sort(notes, comp);
//                break;
//            case Date:
//                comp = new Comparator<NoteItem>() {
//                    @Override
//                    public int compare(NoteItem noteItem, NoteItem t1) {
//                        return 0;
//                    }
//                };
//                Collections.sort(notes, comp);
//                break;
//            case Id:
//                comp = new Comparator<NoteItem>() {
//                    @Override
//                    public int compare(NoteItem a, NoteItem b) {
//                        return a.getId() - b.getId();
//                    }
//                };
//                Collections.sort(notes, comp);
//                break;
//            case Remembered:
//                    notes = restoreOrder(notes);
//                break;
//        }
//        noteAdapter.updateDataSet(notes, animated);
//
//        orderIds = new LinkedList<>();
//        for(int i = 0; i < notes.size(); i++){
//            orderIds.add(notes.get(i).getId());
//        }
//
//    }





}



//save for later

// OnItemClick:
//                NoteItem item = noteAdapter.getItem(position);
//                Cursor c = item.getCursor();
//                //start the appropriate activity
//                switch (c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
//                    case DbContract.NoteEntry.TYPE_TEXT:
//                        Intent i = new Intent(getActivity().getApplication(), TextNoteActivity.class);
//                        i.putExtra(TextNoteActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
//                        startActivity(i);
//                        break;
//                    case DbContract.NoteEntry.TYPE_AUDIO:
//                        Intent i2 = new Intent(getActivity().getApplication(), AudioNoteActivity.class);
//                        i2.putExtra(AudioNoteActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
//                        startActivity(i2);
//                        break;
//                    case DbContract.NoteEntry.TYPE_SKETCH:
//                        Intent i3 = new Intent(getActivity().getApplication(), SketchActivity.class);
//                        i3.putExtra(SketchActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
//                        startActivity(i3);
//                        break;
//                    case DbContract.NoteEntry.TYPE_CHECKLIST:
//                        Intent i4 = new Intent(getActivity().getApplication(), ChecklistNoteActivity.class);
//                        i4.putExtra(ChecklistNoteActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
//                        startActivity(i4);
//                        break;
//                }
