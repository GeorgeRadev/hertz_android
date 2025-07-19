package org.hertz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class FragmentSequences extends Fragment implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SearchView.OnQueryTextListener {

    protected ListView listSequences;
    protected FragmentSequencesAdapter adapter;
    protected SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sequences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchView = (SearchView) view.findViewById(R.id.searchSequences);
        searchView.clearFocus();

        listSequences = (ListView) view.findViewById(R.id.listSequences);
        adapter = new FragmentSequencesAdapter(getActivity(), R.layout.fragment_sequences_row);
        listSequences.setAdapter(adapter);
        listSequences.setOnItemClickListener(this);
        listSequences.setOnItemLongClickListener(this);
        listSequences.setLongClickable(true);

        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Sequence sequence = SequencesManager.sequences.get(position);
        SequencesManager.setSequenceView(sequence);
        Intent intent = new Intent(getActivity(), SequenceView.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public static class DeleteFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Remove from favorites ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(getActivity(), "Clicked: " + id, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            return builder.create();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//        FragmentFavorites.DeleteFragment dialog = new FragmentFavorites.DeleteFragment();
//        dialog.show(getChildFragmentManager(), "DELETE_DIALOG");
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // not needed
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText != null && newText.length() > 1) {
            adapter.setFilter(newText);
        } else {
            adapter.setFilter(null);
        }
        return true;
    }

}