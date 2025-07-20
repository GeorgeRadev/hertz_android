package org.hertz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StringAdapter extends ArrayAdapter<String> {
    private int resource;
    public int selection;

    public StringAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        view = layoutInflater.inflate(resource, parent, false);
        TextView text = view.findViewById(R.id.string);
        text.setText(getItem(position) + " Hz");
        if(position == selection){
            view.setBackgroundColor(0xFF3668a2);
            text.setTextColor(0xFFFFFFFF);
        } else {
            view.setBackgroundColor(0xFFFFFFFF);
            text.setTextColor(0xFF555555);
        }
        return view;
    }
}
