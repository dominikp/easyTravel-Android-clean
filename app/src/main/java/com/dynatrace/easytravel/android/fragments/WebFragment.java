package com.dynatrace.easytravel.android.fragments;

import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.dynatrace.easytravel.android.R;
import com.dynatrace.easytravel.android.application.EasyTravelApplication;
import com.dynatrace.easytravel.android.util.EasyTravelSettings;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebFragment extends Fragment {

    // All constants needed for the URL
    public static final String SPECIAL_OFFER = "specialOffers";
    public static final String CONTACT = "contact";
    public static final String LEGAL = "legal";
    public static final String PRIVACY = "privacy";

    @BindView(R.id.webView)
    WebView mWebView;

    @BindView(R.id.webViewProgress)
    ProgressBar mProgressWebView;



    /**
     * Contains the constant which knows which URL should be opened by the webview
     */
    String mURL;

    /**
     * Application link
     */
    private EasyTravelApplication mApp;

    private Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.bind(this, v);

        // Get the URL
        mApp = (EasyTravelApplication) getActivity().getApplication();
        mURL = getArguments().getString("url");

        // Setup WebView
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setGeolocationDatabasePath("/data/data/com.dynatrace.easytravel.android");
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);


        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        mWebView.loadData("<html><head><title>Loading</title></head><body><p align=\"center\"><font color=\"orange\" size=50><b><i>loading...</i></b></font></p></body></html>", "text/html", "UTF8");

        if (mURL == CONTACT) {
            mWebView.loadUrl(EasyTravelSettings.getServerHostName(getActivity()) + ":" + EasyTravelSettings.getServerPort(getActivity()) + "/contact-orange-mobile.jsf");
        } else if (mURL == LEGAL) {
            mWebView.loadUrl(EasyTravelSettings.getServerHostName(getActivity()) + ":" + EasyTravelSettings.getServerPort(getActivity()) + "/legal-orange-mobile.jsf");
        } else if (mURL == PRIVACY) {
            mWebView.loadUrl(EasyTravelSettings.getServerHostName(getActivity()) + ":" + EasyTravelSettings.getServerPort(getActivity()) + "/privacy-orange-mobile.jsf");
        } else if (mURL == SPECIAL_OFFER) {
            mWebView.loadUrl(EasyTravelSettings.getServerHostName(getActivity()) + ":" + EasyTravelSettings.getServerPort(getActivity()) + "/CreateSpecialOffers");
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO: (5) create custom user action
                    DetailJourneyFragment frag = new DetailJourneyFragment();
                    Bundle journeyBundle = new Bundle();
                    Calendar calStart  = Calendar.getInstance();
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTimeInMillis(new Date().getTime() + 14 * 24 *60 * 60 * 1000);
                    calEnd.setTime(new Date());
                    journeyBundle.putString("journeyFromDate", String.format("%02d", calStart.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", calStart.get(Calendar.MONTH) + 1) + "." + calStart.get(Calendar.YEAR));
                    journeyBundle.putString("journeyToDate", String.format("%02d", calEnd.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", calEnd.get(Calendar.MONTH) + 1) + "." + calEnd.get(Calendar.YEAR));
                    journeyBundle.putString("journeyName", "Limited special offer");
                    journeyBundle.putDouble("journeyAmount", 1399);
                    journeyBundle.putString("journeyPicture", "");
                    journeyBundle.putString("journeyID", "1");
                    frag.setArguments(journeyBundle);
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragmentFrame, frag);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    return true;
                }
            });
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mWebView.getContentHeight() > 0) {
                    mProgressWebView.setVisibility(View.GONE);
                    mWebView.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(this);
                } else {
                    mHandler.postDelayed(this, 100);
                }
            }
        }, 100);
        return v;
    }


}
