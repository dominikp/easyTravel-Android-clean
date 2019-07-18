package com.dynatrace.easytravel.android.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.annotation.Nullable;
import android.util.Patterns;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.dynatrace.easytravel.android.R;
import com.dynatrace.easytravel.android.util.EasyTravelSettings;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference hostPref = findPreference(EasyTravelSettings.KEY_SERVER_HOSTNAME);
        Preference portPref = findPreference(EasyTravelSettings.KEY_SERVER_PORT);

        hostPref.setSummary(EasyTravelSettings.getServerHostName(getActivity()));
        hostPref.setOnPreferenceChangeListener(this);
        portPref.setSummary(EasyTravelSettings.getServerPort(getActivity()));
        portPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case EasyTravelSettings.KEY_SERVER_HOSTNAME:
                findPreference(key).setSummary(EasyTravelSettings.getServerHostName(getActivity()));
                break;
            case EasyTravelSettings.KEY_SERVER_PORT:
                findPreference(key).setSummary(EasyTravelSettings.getServerPort(getActivity()));
                break;
        }

        sharedPreferences.edit().apply();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean success = false;

        switch (preference.getKey()) {
            case EasyTravelSettings.KEY_SERVER_HOSTNAME:
                success = validateHostName((String) o);
                break;

            case EasyTravelSettings.KEY_SERVER_PORT:
                success = validatePort((String) o);
                break;
        }

        if (!success) {
            Toast.makeText(getActivity(), getString(R.string.preference_validation_error, o.toString(), preference.getTitle()), Toast.LENGTH_SHORT).show();
        }

        return success;
    }

    private boolean validateHostName(String hostName) {
        return Patterns.WEB_URL.matcher(hostName).matches() && URLUtil.isValidUrl(hostName);
    }

    private boolean validatePort(String port) {
        try {
            int portNumber = Integer.parseInt(port);
            if (portNumber >= 0 && portNumber <= 65535) {
                return true;
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        return false;
    }
}
