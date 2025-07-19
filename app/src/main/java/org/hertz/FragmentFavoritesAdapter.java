package org.hertz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.hertz.R;

public class FragmentFavoritesAdapter extends BaseAdapter {

    private Context context;
    private int resource;

    public FragmentFavoritesAdapter(@NonNull Context context, int resource) {
        this.context = context;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return SequencesManager.favorites.size();
    }

    @Override
    public String getItem(int position) {
        return SequencesManager.favorites.get(position);
    }

    @Override
    public long getItemId(int position) {
        return SequencesManager.favorites.get(position).hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(resource, parent, false);
        {
            TextView text = view.findViewById(R.id.favoritesText);
            text.setText(getItem(position));
        }
        return view;
    }
}