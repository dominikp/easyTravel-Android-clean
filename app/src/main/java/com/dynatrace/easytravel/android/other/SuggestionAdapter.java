package com.dynatrace.easytravel.android.other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dynatrace.easytravel.android.R;

import java.util.ArrayList;

/**
 * Created by Matthias Hochrieser
 * List which contains the suggestions that will be presented when
 * the user is entering a few characters
 */
public class SuggestionAdapter extends ArrayAdapter<String> {

    /**
     * All suggestions which should be displayed in the list
     */
    String[] mSuggestions;

    /**
     * Adapter which holds the suggestions in the list
     * @param context Context of adapter
     * @param objects All suggestion strings
     */
    public SuggestionAdapter(Context context, String[] objects) {
        super(context, 0, objects);
        mSuggestions = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String destination = mSuggestions[position];

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.suggestion_entry, parent, false);
        }

        // Set the string of the suggestion
        TextView tvName = (TextView) convertView.findViewById(R.id.suggestionEntry);
        tvName.setText(destination);

        return convertView;
    }

    @Override
    public String getItem(int position) {
        return mSuggestions[position];
    }
}