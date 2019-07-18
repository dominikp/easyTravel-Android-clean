package com.dynatrace.easytravel.android.other;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dynatrace.easytravel.android.R;
import com.dynatrace.easytravel.android.data.Journey;
import com.dynatrace.easytravel.android.fragments.ResultsFragment;
import com.dynatrace.easytravel.android.interfaces.OnJourneySelectedListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Matthias Hochrieser
 * This adapter contains a list full of journeys
 */
public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    /**
     * All Journeys that are in the View
     */
    private List<Journey> mListJourney;

    /**
     * Listener which returns the selected journey from the list
     */
    private final OnJourneySelectedListener mListener;

    /**
     * View Holder object for the recycler view which presents the journey information
     */
    public static class JourneyViewHolder extends RecyclerView.ViewHolder{

        // All UI elements for the journey
        @BindView(R.id.descriptionJourney) TextView mDescription;
        @BindView(R.id.nameJourney) TextView mName;
        @BindView(R.id.cardResult) CardView mCard;
        @BindView(R.id.pictureJourney) ImageView mPicture;
        @BindView(R.id.priceJourney) TextView mPrice;
        @BindView(R.id.fromJourney) TextView mFrom;
        @BindView(R.id.toJourney) TextView mTo;
        @BindView(R.id.buttonBook) Button mButtonBook;

        JourneyViewHolder(View item){
            super(item);
            ButterKnife.bind(this, item);
        }
    }

    /**
     * Constructor of the journey adapter
     * @param _journeys All journeys which should be displayed by the recycler view
     * @param _listener Listener for selecting a journey
     */
    public JourneyAdapter(List<Journey> _journeys, OnJourneySelectedListener _listener){
        mListJourney = _journeys;
        mListener = _listener;
    }

    @Override
    public JourneyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journey_card, parent, false);
        JourneyViewHolder holder = new JourneyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(JourneyViewHolder holder, final int position) {
        // Set Data
        Journey journey = mListJourney.get(position);

        // Set all Information
        holder.mDescription.setText("If you wish to stay in a hotel that has friendly staff and an inviting ambience that reminds you of home, then head to this Hotel.");
        holder.mName.setText(journey.getName());
        holder.mFrom.setText(journey.getFromDate());
        holder.mTo.setText(journey.getToDate());
        holder.mPrice.setText(String.valueOf(journey.getAmount()) + " $");

        // Convert image if it is available
        if(journey.getImage() != null){
            holder.mPicture.setImageBitmap(journey.getImage());
        }

        // Button will fire the onjourneyselectedlistener
        holder.mButtonBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to Booking Screen
                mListener.onJourneySelected(mListJourney.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListJourney.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
