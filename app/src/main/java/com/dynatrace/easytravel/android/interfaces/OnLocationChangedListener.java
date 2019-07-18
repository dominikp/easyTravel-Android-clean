package com.dynatrace.easytravel.android.interfaces;

import android.location.Location;

/**
 * Created by Matthias Hochrieser
 * Location changed in the GPS tracker and is notifying other activities
 */
public interface OnLocationChangedListener {

    /**
     * Notfiy others that the GPS location has changed
     * @param _location New GPS location
     */
    public void locationChanged(Location _location);
}
