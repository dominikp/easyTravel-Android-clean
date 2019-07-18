package com.dynatrace.easytravel.android.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.dynatrace.easytravel.android.R;
import com.dynatrace.easytravel.android.application.EasyTravelApplication;
import com.dynatrace.easytravel.android.data.Journey;
import android.location.Location;
import android.widget.EditText;

import com.dynatrace.easytravel.android.fragments.DetailJourneyFragment;
import com.dynatrace.easytravel.android.fragments.ResultsFragment;
import com.dynatrace.easytravel.android.fragments.SearchFragment;
import com.dynatrace.easytravel.android.fragments.SettingsFragment;
import com.dynatrace.easytravel.android.fragments.UserFragment;
import com.dynatrace.easytravel.android.fragments.WebFragment;
import com.dynatrace.easytravel.android.interfaces.OnJourneySelectedListener;
import com.dynatrace.easytravel.android.interfaces.OnLocationChangedListener;
import com.dynatrace.easytravel.android.interfaces.OnResultsReturnedListener;
import com.dynatrace.easytravel.android.other.GPSTracker;

import java.util.Vector;

import butterknife.ButterKnife;

/**
 * Main Activity of the Easy Travel App
 * Author: Matthias Hochrieser
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnResultsReturnedListener, FragmentManager.OnBackStackChangedListener, OnJourneySelectedListener, OnLocationChangedListener{

	private ActionBarDrawerToggle mDrawerToggle;
	private static final int MY_PERMISSION_LOCATION = 1;
	private GPSTracker mTracker;
	private EasyTravelApplication mApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Navigation Drawer
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);

				// Hide the keyboard
				View v = getCurrentFocus();

				if(v != null){
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		};
		drawer.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		// Set Item at startup
		navigationView.getMenu().getItem(0).setChecked(true);

		mApp = (EasyTravelApplication) this.getApplication();

		// Set Search Fragment
		SearchFragment fragment = new SearchFragment();
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragmentFrame, fragment);
		fragmentTransaction.commit();

		getSupportFragmentManager().addOnBackStackChangedListener(this);

		//Handle when activity is recreated like on orientation Change
		shouldDisplayHomeUp();

		askForLocationPermission();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mTracker.stopUsingGPS();
	}

	@Override
	protected void onPause() {
		mTracker.stopUsingGPS();
		super.onPause();
	}

	@Override
	protected void onResume() {
		initLocationService();
		super.onResume();
	}

	public void askForLocationPermission(){
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_LOCATION);
		}
	}

	public void initLocationService(){
		mTracker = new GPSTracker(this);

		if(mTracker.canGetLocation()){
			Location location = mTracker.getLocation();
			if(location != null){
				//DynatraceUEM.setGpsLocation(location);
			}
		}else{
			//DynatraceUEM.reportError("could not access GPS location", -1);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case MY_PERMISSION_LOCATION: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initLocationService();
				} else {
					//DynatraceUEM.reportError("could not access GPS location", -1);
				}
				return;
			}
		}
	}

	@Override
	public void onBackStackChanged() {
		shouldDisplayHomeUp();
	}

	public void shouldDisplayHomeUp(){
		//Enable Up button only  if there are entries in the back stack
		if (getSupportActionBar() != null) {
			if (getSupportFragmentManager().getBackStackEntryCount()>0) {
				mDrawerToggle.setDrawerIndicatorEnabled(false);
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			} else {
				getSupportActionBar().setDisplayHomeAsUpEnabled(false);
				mDrawerToggle.setDrawerIndicatorEnabled(true);
			}
		}
	}

	@Override
	public boolean onSupportNavigateUp() {
		//This method is called when the up button is pressed. Just the pop back stack.
		getSupportFragmentManager().popBackStack();
		return true;
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

		View v = getCurrentFocus();

		if(v != null){
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}

		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.option_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.flushEvents:
				//DynatraceUEM.flushEvents();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		Fragment frag = null;

		if (id == R.id.nav_search) {
			frag = new SearchFragment();
		} else if (id == R.id.nav_user) {
			frag = new UserFragment();
		} else if (id == R.id.nav_offers) {
			Bundle args = new Bundle();
			args.putString("url", WebFragment.SPECIAL_OFFER);
			frag = new WebFragment();
			frag.setArguments(args);
		} else if (id == R.id.nav_terms) {
			Bundle args = new Bundle();
			args.putString("url", WebFragment.LEGAL);
			frag = new WebFragment();
			frag.setArguments(args);
		} else if (id == R.id.nav_privacy) {
			Bundle args = new Bundle();
			args.putString("url", WebFragment.PRIVACY);
			frag = new WebFragment();
			frag.setArguments(args);
		} else if (id == R.id.nav_contact) {
			Bundle args = new Bundle();
			args.putString("url", WebFragment.CONTACT);
			frag = new WebFragment();
			frag.setArguments(args);
		} else if (id == R.id.nav_settings){
			frag = new SettingsFragment();
		}

		if(frag != null){
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.fragmentFrame, frag);
			fragmentTransaction.commit();
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void onResultsReturned(Vector<Journey> _results) {
		// Hide the Keyboard
		View v = getCurrentFocus();

		if(v != null){
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}

		// Results from the Search Fragment
		ResultsFragment frag = new ResultsFragment();
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragmentFrame, frag);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onJourneySelected(Journey _journey) {
		// Journey was selected in the result list
		DetailJourneyFragment frag = new DetailJourneyFragment();
		Bundle journeyBundle = new Bundle();
		journeyBundle.putString("journeyFromDate", _journey.getFromDate());
		journeyBundle.putString("journeyToDate", _journey.getToDate());
		journeyBundle.putString("journeyName", _journey.getName());
		journeyBundle.putDouble("journeyAmount", _journey.getAmount());
		journeyBundle.putString("journeyPicture", _journey.getImageEnc());
		journeyBundle.putString("journeyID", _journey.getId());
		frag.setArguments(journeyBundle);

		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragmentFrame, frag);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void locationChanged(Location _location) {
		if(_location != null){
			//DynatraceUEM.setGpsLocation(_location);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handleReturn = super.dispatchTouchEvent(ev);

		View view = getCurrentFocus();

		int x = (int) ev.getX();
		int y = (int) ev.getY();

		if(view instanceof EditText){
			View innerView = getCurrentFocus();

			if (ev.getAction() == MotionEvent.ACTION_UP &&
					!getLocationOnScreen(innerView).contains(x, y)) {

				InputMethodManager input = (InputMethodManager)
						getSystemService(Context.INPUT_METHOD_SERVICE);
				input.hideSoftInputFromWindow(getWindow().getCurrentFocus()
						.getWindowToken(), 0);
			}
		}

		return handleReturn;
	}

	protected Rect getLocationOnScreen(View mView) {
		Rect mRect = new Rect();
		int[] location = new int[2];

		mView.getLocationOnScreen(location);

		mRect.left = location[0];
		mRect.top = location[1];
		mRect.right = location[0] + mView.getWidth();
		mRect.bottom = location[1] + mView.getHeight();

		return mRect;
	}
}
