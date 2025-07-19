package org.hertz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FragmentSequencesAdapter extends BaseAdapter {

    private Context context;
    private int resource;
    private String filter;
    private final List<Integer> filtered;

    public FragmentSequencesAdapter(@NonNull Context context, int resource) {
        this.context = context;
        this.resource = resource;
        filter = null;
        filtered = new ArrayList<Integer>(SequencesManager.sequences.size());
    }

    @Override
    public int getCount() {
        if (filter != null) {
            return filtered.size();
        } else {
            return SequencesManager.sequences.size();
        }
    }

    @Override
    public Sequence getItem(int position) {
        if (filter != null) {
            position = filtered.get(position);
        }
        return SequencesManager.sequences.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).name.hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(resource, parent, false);
        {
            TextView text = view.findViewById(R.id.sequencesText);
            text.setText(getItem(position).name);
        }
        if (filter == null) {
            TextView text = view.findViewById(R.id.sequencesDescription);
            text.setVisibility(View.GONE);
        } else {
            TextView text = view.findViewById(R.id.sequencesDescription);
            text.setText(getItem(position).description);
        }
        return view;
    }

    public void setFilter(String text) {
        if (text != null && text.length() <= 0) {
            text = null;
        }
        filter = text;

        if (filter != null) {
            SequencesManager.sequencesFilter(filter, filtered);
        }
        notifyDataSetChanged();
    }
}