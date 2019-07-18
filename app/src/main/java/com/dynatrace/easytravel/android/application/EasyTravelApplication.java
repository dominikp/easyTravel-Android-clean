package com.dynatrace.easytravel.android.application;

import android.app.Application;

import com.dynatrace.easytravel.android.data.Journey;

import java.util.Vector;

/**
 * @author richard.vogl
 */
public class EasyTravelApplication extends Application {

    private String loggedInUser = null;
    private Vector<Journey> results;

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(String user) {
        loggedInUser = user;
    }

    public Vector<Journey> getResults() {
        return results;
    }

    public void setResults(Vector<Journey> results) {
        this.results = results;
    }
}
