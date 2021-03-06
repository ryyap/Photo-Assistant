package com.example.photoassistant;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Lens is a fragment in which is swapped into the main activity screen
 * which controls the lens handling UI and selections.
 */
public class Lens extends Fragment {

    private static ArrayAdapter<String> arrayAdapterString;
    ListItem slot[];
    int currentPosSelcted;
    private RecyclerView recyclerView;
    private RecyclerAdapterListItem ra;
    private Spinner lens_spinner;
    private ArrayList<String> favouritesString;
    private ArrayList<ListItem> favourites;
    private ArrayList<ListItem> arrayToSort;
    private int currentSlot;


    public Lens() {
        // Required empty public constructor
    }


    public Lens(ListItem[] slot, ArrayList<ListItem> lia, int whichSlot) {

        this.slot = slot;
        arrayToSort = lia;
        currentSlot = whichSlot;
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_lens, container, false);
        return rootView;

    }

    /**
     * this method updates the array adapter, as well updating
     * the spinner adapters using Favourites string.
     */

    public void updateArrayAdapter() {

        favouritesString.clear();
        int increasedi = 1;
        for (int i = 0; i < favourites.size(); i++) {

            favouritesString.add(i, favourites.get(i).getPartName());
            Log.d("SlotDebug", "updateArrayAdapter: " + slot[increasedi] + " length - " + slot.length + " i - " + i);
            slot[increasedi] = favourites.get(i);
            increasedi++;
        }
        arrayAdapterString.notifyDataSetChanged();

    }

    /**
     * this method adds a new lens to the array. if there is less than
     *  howManySsavedLenses (= 5 )it  just adds them, until there is
     *  more than 5 where it uses FIFO to add.
     *
     * @param lensToAdd
     */
    public void addLensToArrays(ListItem lensToAdd) {
        Log.d("addLensToArray", "" + favourites.size());
        if (!favourites.contains(lensToAdd)) {
            if (favourites.size() < BodySelector.howManySsavedLenses) {
                favourites.add(0, lensToAdd);
            } else {
                favourites.add(0, lensToAdd);
                favourites.remove(BodySelector.howManySsavedLenses);

            }
        }

    }

    /**
     * this is used to swap the currently selected lens to the position at the
     * start of the array.
     * @param position is the position currently selected to swap.
     */
    private void swapSpinnerItemToStart(int position) {

        if (slot[1] != null && slot[2] != null && position != 0) { // if the first spots arent empty

            Collections.swap(favourites, position, 0);
            lens_spinner.getSelectedItem();
            updateArrayAdapter();

        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        favourites = new ArrayList<ListItem>();
        favouritesString = new ArrayList<String>();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        swapSpinnerItemToStart(currentPosSelcted);

        super.onDestroy();
    }

    /**
     * this is a lifecycle call which is called when the view is created.
     * this is where the lens spinner and recyclerView are instantiated
     * and the ui elements are bound.
     *
     * @param view the view in which the layout can be inflated in
     * @param savedInstanceState if any data was passed in, retrieve it through this
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lens_spinner = view.findViewById(R.id.spinner1);
        arrayAdapterString = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, favouritesString);
        lens_spinner.setAdapter(arrayAdapterString);
        updateArrayAdapter();
        recyclerView = (RecyclerView) view.findViewById(R.id.lens_rv);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // use a linear layout manager
        RecyclerView.LayoutManager loutmn = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(loutmn);

        final Button doneButton = getActivity().findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favourites.size() > 0) {
                    BodySelector.addSlot(currentSlot, slot);
                    MainActivity.fragmentStack.pop();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fl, new BodySelector()).commit();
                }
            }
        });

        /**
         * Instantiating the recycler view apdapter class RecyclerAdapterListItem
         * passes in an onItemClick which is used to handle what happens with
         * a click of a list item in the recycler view through RecyclerViewOnClickListener
         * interface. it passes back an instance of what is clicked and then its used
         * to instantiate Lens when ready.
         */
        ra = new RecyclerAdapterListItem(getContext(), arrayToSort, new RecyclerViewOnClickListener() {
            @Override
            public void onItemClick(ListItem item) {
                doneButton.setVisibility(View.VISIBLE);
                addLensToArrays(item);
                updateArrayAdapter();

            }
        });

        recyclerView.setAdapter(ra);
        ra.notifyDataSetChanged();


        lens_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPosSelcted = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        super.onViewCreated(view, savedInstanceState);

    }


    /**
     * this object handles the menu options, which is used for the
     * seach function.
     *
     * @param menu an instance of menu passed in from super
     * @param inflater instance of MenuInflater passed in from super
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.search_bar, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_bar));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;

            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (false) ;//(newText.length() == 0 || newText.isEmpty()){}
                else {

                    ra.getFilter().filter(newText.toLowerCase().trim());
                }
                return true;
            }
        });
    }


}
