package org.secuso.privacyfriendlynotes.util;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class OrderedAdapter<T extends IFlexible> extends FlexibleAdapter<T>{

    public OrderedAdapter(@Nullable List<T> items) {
        super(items);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //TODO
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO
    }

    public void sortItems(Comparator<T> comp){
        List<T> current = new LinkedList<>();
        current.addAll(getCurrentItems());

        Collections.sort(current, comp);
        reArrangeItems(current);
    }

    /**
     * assumes the resulting list contains the same items as the current one
     * @param resultList
     */
    public void reArrangeItems(List<T> resultList){
        for(int i = 0; i < resultList.size(); i++){
            int tmp = getGlobalPositionOf(resultList.get(i));
            if (tmp != -1)
                moveItem(tmp, i);
        }
    }

    //TODO: Maybe make an easy animated option to add and update items that are inserted with animation
    // call updateDataSet(items, false) to correctly
    // do not overwrite updateDataSet() to avoid confusion
    public void updateDataSetAnimated(@Nullable List<T> items){
        updateDataSet(items, false);
    }

}
