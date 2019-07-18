package com.dynatrace.easytravel.android.interfaces;

import com.dynatrace.easytravel.android.data.Journey;

/**
 * Created by Matthias Hochrieser
 * Listener for returning the Journey which was selected in the recycler view
 */
public interface OnJourneySelectedListener {

    /**
     * A journey entry was selected in the recycler view
     * @param _journey Journey which was selected
     */
    public void onJourneySelected(Journey _journey);

}
