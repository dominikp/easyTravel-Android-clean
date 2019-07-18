package com.dynatrace.easytravel.android.fragments;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dynatrace.easytravel.android.R;
import com.dynatrace.easytravel.android.application.EasyTravelApplication;
import com.dynatrace.easytravel.android.data.Journey;
import com.dynatrace.easytravel.android.interfaces.OnResultsReturnedListener;
import com.dynatrace.easytravel.android.other.SuggestionAdapter;
import com.dynatrace.easytravel.android.rest.RestJourney;
import com.dynatrace.easytravel.android.rest.RestSearch;
import com.dynatrace.easytravel.android.util.EasyTravelSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Matthias Hochrieser
 * Fragment which displays the search for vacation goals
 */
public class SearchFragment extends Fragment implements ListView.OnItemClickListener{

    private static final String NO_JOURNEYS_FOUND_ACTION_NAME = "NoJourneysFound";
    private static final String JOURNEYS_FOUND_ACTION_NAME = "JourneysFound";
    private static final String SEARCH_JOURNEY_ACTION_NAME = "searchJourney";

    @BindView(R.id.buttonSearchDest)
    Button mButtonSearch;

    @BindView(R.id.buttonFrom)
    Button mButtonFrom;

    @BindView(R.id.buttonTo)
    Button mButtonTo;

    @BindView(R.id.editDestination)
    EditText mEditDestination;

    @BindView(R.id.searchResults)
    ListView mListResults;

    @BindView(R.id.searchProgress)
    ProgressBar mProgressSearch;

    @BindView(R.id.cardSuggestions)
    CardView mCardSuggestions;

    @BindView(R.id.resultMessage)
    TextView mResultMessage;

    private EasyTravelApplication mApp;
    SuggestionAdapter mSuggestionAdapter;
    Calendar mCal;

    OnResultsReturnedListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, null);
        ButterKnife.bind(this, root);

        mApp = (EasyTravelApplication) getActivity().getApplication();

        // Set Date in Button Title
        mCal = Calendar.getInstance();
        mButtonFrom.setText(String.format("%02d", mCal.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", (mCal.get(Calendar.MONTH))) + "." + mCal.get(Calendar.YEAR));
        mButtonTo.setText(  String.format("%02d", mCal.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", (mCal.get(Calendar.MONTH) + 6)) + "." + mCal.get(Calendar.YEAR));

        mListResults.setOnItemClickListener(this);

        mEditDestination.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Start Suggestions Task
                new AsyncSuggestions(s.toString()).execute();
            }
        });

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnResultsReturnedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnResultsReturnedListener");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView tvName = (TextView) view.findViewById(R.id.suggestionEntry);
        // Set Suggestion in Textfield
        mEditDestination.setText(tvName.getText());
        // Set Cursor to the End
        mEditDestination.setSelection(mEditDestination.getText().length());
    }

    @OnClick({R.id.buttonFrom, R.id.buttonTo})
    public void openDatePicker(final Button _btn){
        if(getActivity() != null){
            View v = getActivity().getCurrentFocus();

            if(v != null){
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                _btn.setText(String.format("%02d", dayOfMonth) + "." + String.format("%02d", (monthOfYear + 1)) + "." + year);
            }
        }, mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @OnClick(R.id.buttonSearchDest)
    public void searchDestination(Button _btn){
        // Turn On Progress Bar
        mProgressSearch.setVisibility(View.VISIBLE);

        new AsyncResults().execute();
    }

    private class AsyncResults extends AsyncTask<Void, Void, Vector<Journey>> {

        String mDestination;
        Date mDateFrom;
        Date mDateTo;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String dateStrFrom = mButtonFrom.getText().toString();
            String dateStrTo = mButtonTo.getText().toString();

            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

            try {
                mDateFrom = format.parse(dateStrFrom);
                mDateTo = format.parse(dateStrTo);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            if (mSuggestionAdapter != null) {
                mDestination = mSuggestionAdapter.getItem(0);
            }
            if (mDestination == null) {
                mDestination = mEditDestination.getText().toString();
            }
        }

        @Override
        protected Vector<Journey> doInBackground(Void... params) {
            //UemAction searchAction = DynatraceUEM.enterAction(SEARCH_JOURNEY_ACTION_NAME);

            try {
                if(mDateFrom != null && mDateTo != null){
                    RestJourney journey = new RestJourney(EasyTravelSettings.getServerHostName(getActivity()),
                            Integer.valueOf(EasyTravelSettings.getServerPort(getActivity())),
                            mDestination,
                            mDateFrom.getTime(),
                            mDateTo.getTime());
                    //journey.setParentAction(searchAction);
                    ArrayList<RestJourney.JourneyRecord> records = journey.performSearch();

                    if(EasyTravelSettings.shouldHaveErrorOnBookingAndSearch(getActivity())){
                        //searchAction.reportError("failed to display Ad", -3546);
                    }

                    if (journey.hasError()) {
                        //searchAction.reportEvent(NO_JOURNEYS_FOUND_ACTION_NAME);
                        return null;
                    } else if (records != null && records.size() > 0 ) {
                        final ArrayList<RestJourney.JourneyRecord> finalRecords = new ArrayList<RestJourney.JourneyRecord>(records);
                        //searchAction.reportValue(JOURNEYS_FOUND_ACTION_NAME, finalRecords.size());

                        Vector<Journey> dataJourneys = new Vector<Journey>();
                        for (RestJourney.JourneyRecord record : finalRecords){
                            dataJourneys.add(new Journey(record));
                        }

                        return dataJourneys;
                    } else {
                        //searchAction.reportEvent(NO_JOURNEYS_FOUND_ACTION_NAME);
                        return null;
                    }
                }else{
                    // No Date
                    return null;
                }
            } finally {
                //searchAction.leaveAction();
            }
        }

        @Override
        protected void onPostExecute(Vector<Journey> s) {
            super.onPostExecute(s);

            mProgressSearch.setVisibility(View.GONE);

            if(s == null){
                // No Results
                mResultMessage.setVisibility(View.VISIBLE);
            }else if(s.size() > 0){
                // Results
                mApp.setResults(s);
                mCallback.onResultsReturned(s);
                mResultMessage.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Async Task which downloads Suggestions
     */
    private class AsyncSuggestions extends AsyncTask<Void, Void, String[]> {

        String mSuggestion;

        public AsyncSuggestions(String _suggestion){
            mSuggestion = _suggestion;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressSearch.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(Void... params) {
            RestSearch search = new RestSearch(EasyTravelSettings.getServerHostName(getActivity()),
                    Integer.valueOf(EasyTravelSettings.getServerPort(getActivity())),
                    mSuggestion,
                    true);
            ArrayList<RestSearch.SearchRecord> records = search.performSearch();

            String[] suggestions = new String[records.size()];
            for(int i = 0; i < records.size(); i++){
                suggestions[i] = records.get(i).name;
            }

            return suggestions;
        }

        @Override
        protected void onPostExecute(String[] searchRecords) {
            super.onPostExecute(searchRecords);

            // List View
            if(getActivity() != null){
                mSuggestionAdapter = new SuggestionAdapter(getActivity(), searchRecords);
                mListResults.setAdapter(mSuggestionAdapter);
                mCardSuggestions.setVisibility(View.VISIBLE);
            }

            if(searchRecords == null || searchRecords.length == 0){
                mCardSuggestions.setVisibility(View.GONE);
            }

            mProgressSearch.setVisibility(View.GONE);
        }
    }
}
