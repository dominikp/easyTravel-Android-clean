package com.dynatrace.easytravel.android.interfaces;

import com.dynatrace.easytravel.android.data.Journey;

import java.util.Vector;

/**
 * Created by Matthias Hochrieser
 * Returns the Results that were requested from the server
 */
public interface OnResultsReturnedListener {

    /**
     * Return the results (journeys) which were found on the server
     * @param _results All journeys
     */
    public void onResultsReturned(Vector<Journey> _results);

}
