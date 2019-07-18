package com.dynatrace.easytravel.android.fragments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.dynatrace.easytravel.android.R;
import com.dynatrace.easytravel.android.util.EasyTravelSettings;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Matthias Hochrieser
 * Fragment which is displaying a webview
 */
public class WebFragment extends Fragment {

    // All constants needed for the URL
    public static final String SPECIAL_OFFER = "specialOffers";
    public static final String CONTACT = "contact";
    public static final String LEGAL = "legal";
    public static final String PRIVACY = "privacy";

    @BindView(R.id.webView) WebView mWebView;
    @BindView(R.id.webViewProgress) ProgressBar mProgressWebView;

    /**
     * Contains the constant which knows which URL should be opened by the webview
     */
    String mURL;

    private Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_web,container,false);
        ButterKnife.bind(this, v);

        // Get the URL
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

        // Determine Action
        if(mURL == SPECIAL_OFFER){
            mWebView.loadUrl("file:///android_asset/special_offers.html");
            mWebView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url){
                    mWebView.loadUrl("javascript:createEasyTravelBaseURL('" + EasyTravelSettings.getServerHostName(getActivity()) +"', '" + EasyTravelSettings.getServerPort(getActivity()) +"')");
                    mProgressWebView.setVisibility(View.GONE);
                }
            });
        }else{

            mWebView.loadData("<html><head><title>Loading</title></head><body><p align=\"center\"><font color=\"orange\" size=50><b><i>loading...</i></b></font></p></body></html>", "text/html", "UTF8");

            String baseUrl = EasyTravelSettings.getServerHostName(getActivity()) +":" + EasyTravelSettings.getServerPort(getActivity());
            if(mURL == CONTACT){
                mWebView.loadUrl(baseUrl + "/contact-orange-mobile.jsf");
            }else if(mURL == LEGAL){
                mWebView.loadUrl(baseUrl + "/legal-orange-mobile.jsf");
            }else if(mURL == PRIVACY){
                //UemAction loadWebview = DynatraceUEM.enterAction("loadWebview");
                mWebView.loadUrl(baseUrl + "/privacy-orange-mobile.jsf");
                //loadWebview.leaveAction();
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
        }

        return v;
    }


}
