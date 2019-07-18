package com.dynatrace.easytravel.android.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dynatrace.easytravel.android.R;
import com.dynatrace.easytravel.android.application.EasyTravelApplication;
import com.dynatrace.easytravel.android.data.Journey;
import com.dynatrace.easytravel.android.interfaces.OnJourneySelectedListener;
import com.dynatrace.easytravel.android.other.JourneyAdapter;

import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Matthias Hochrieser
 * Fragment which displays the Results (journeys)
 */
public class ResultsFragment extends Fragment {

    @BindView(R.id.recyclerResults) RecyclerView mResultList;
    JourneyAdapter mJourneyAdapter;
    Vector<Journey> mResults;
    OnJourneySelectedListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_results,container,false);
        ButterKnife.bind(this, v);

        EasyTravelApplication app = (EasyTravelApplication) getActivity().getApplication();
        mResults = app.getResults();

        // Attach the Main Activity with the Interface OnJourneySelected to Adapter
        mJourneyAdapter = new JourneyAdapter(mResults, mCallback);

        mResultList.setAdapter(mJourneyAdapter);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnJourneySelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnJourneySelectedListener");
        }
    }
}
