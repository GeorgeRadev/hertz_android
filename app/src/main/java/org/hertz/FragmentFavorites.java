package org.hertz;

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
import androidx.fragment.app.Fragment;

public class FragmentFavorites extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    protected ListView listFavorites;
    protected FragmentFavoritesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listFavorites = (ListView) view.findViewById(R.id.listFavorites);
        adapter = new FragmentFavoritesAdapter(getActivity(), R.layout.fragment_favorite_row);
        listFavorites.setAdapter(adapter);
        listFavorites.setOnItemClickListener(this);
        listFavorites.setOnItemLongClickListener(this);
        listFavorites.setLongClickable(true);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String sequenceName = SequencesManager.favorites.get(position);
        Sequence sequence = SequencesManager.getFavoriteSequence(sequenceName);
        if (sequence != null) {
            SequencesManager.setSequenceView(sequence);
            Intent intent = new Intent(getActivity(), SequenceView.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            Toast.makeText(getActivity(), "sequence [" + sequenceName + "] does not exist anymore", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}