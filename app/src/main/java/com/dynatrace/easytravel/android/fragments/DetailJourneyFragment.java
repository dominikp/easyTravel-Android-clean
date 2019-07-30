package com.dynatrace.easytravel.android.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.app.ProgressDialog;

import com.demo.dss.Crash;
import com.dynatrace.easytravel.android.R;
import com.dynatrace.easytravel.android.application.EasyTravelApplication;
import com.dynatrace.easytravel.android.rest.RestBooking;
import com.dynatrace.easytravel.android.util.EasyTravelSettings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailJourneyFragment extends Fragment implements SeekBar.OnSeekBarChangeListener{
    @BindView(R.id.nameInfoJourney) TextView mTextNameJourney;
    @BindView(R.id.fromInfoJourney) TextView mTextDateFromJourney;
    @BindView(R.id.toInfoJourney) TextView mTextDateToJourney;
    @BindView(R.id.textInfoFlightPrice) TextView mTextFlightPrice;
    @BindView(R.id.textInfoHotelPrice) TextView mTextHotelPrice;
    @BindView(R.id.textInfoTaxes) TextView mTextTaxes;
    @BindView(R.id.textInfoPrice) TextView mTextTotalPrice;
    @BindView(R.id.textAdult) TextView mTextAdult;
    @BindView(R.id.textKids) TextView mTextKids;
    @BindView(R.id.textLoginToBook) TextView mTextLoginToBook;

    @BindView(R.id.seekBarAdults) SeekBar mSeekAdults;
    @BindView(R.id.seekBarKids) SeekBar mSeekKids;

    @BindView(R.id.pictureInfoJourney) ImageView mImageJourney;

    @BindView(R.id.buttonInfoBook) Button mButtonBook;

    private double mPriceFlightAdult;
    private double mPriceHotelAdult;
    private final double mPriceKidsPercentage = 0.75;
    private final double mTaxes = 29.90;
    private double mCurrentTotal;

    private int mAmountKids;
    private int mAmountAdult;

    private EasyTravelApplication mApp;

    private ProgressDialog mProgressDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_journey_detail, container, false);
        ButterKnife.bind(this, v);

        mApp = (EasyTravelApplication) getActivity().getApplication();

        mTextNameJourney.setText(getArguments().getString("journeyName"));
        mTextDateFromJourney.setText(getArguments().getString("journeyFromDate"));
        mTextDateToJourney.setText(getArguments().getString("journeyToDate"));

        // Getting the Amount and Calculate Subpositions
        mCurrentTotal = getArguments().getDouble("journeyAmount");
        mPriceFlightAdult = (mCurrentTotal - mTaxes) * 0.3;
        mPriceHotelAdult = (mCurrentTotal - mTaxes) * 0.7;

        mTextFlightPrice.setText(String.format("%.2f $", mPriceFlightAdult));
        mTextHotelPrice.setText(String.format("%.2f $", mPriceHotelAdult));
        mTextTotalPrice.setText(String.format("%.2f $", mCurrentTotal));
        mTextTaxes.setText(String.format("%.2f $", mTaxes));

        String strImage = getArguments().getString("journeyPicture");
        if(strImage != ""){
            mImageJourney.setImageBitmap(decodeBase64(strImage));
        }

        mAmountAdult = 1;
        mAmountKids = 0;

        mSeekAdults.setOnSeekBarChangeListener(this);
        mSeekKids.setOnSeekBarChangeListener(this);

        return v;
    }

    @OnClick(R.id.buttonInfoBook)
    public void onBookClick(Button _btn){
        if(mApp.getLoggedInUser() == null){
            mTextLoginToBook.setVisibility(View.VISIBLE);
        }else{
            mTextLoginToBook.setVisibility(View.GONE);

            mProgressDialog = ProgressDialog.show(getActivity(), "Booking...", "Sending your booking request to easyTravel", true, false);

            //JLT-110432 create credit card number from user name, so it is different for different users
            String name = mApp.getLoggedInUser();
            long value = 0;
            for (char c : name.toCharArray()) {
                value = 31 * value + c;	//ensure same calculation and result on iOS and Android
            }

            String creditCardNumber = Long.toString((value + 1000000000000000l) % 10000000000000000l); //make sure it has exactly 16 digits

            // Make Booking in Background
            new AsyncBooking(creditCardNumber, getArguments().getString("journeyID")).execute();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(seekBar == mSeekAdults){
            mAmountAdult = progress + 1;
            mTextAdult.setText("Adult (" + mAmountAdult + ")");
        }else if(seekBar == mSeekKids){
            mAmountKids = progress;
            mTextKids.setText("Kids (" + mAmountKids + ")");
        }

        double totalFlight = mPriceFlightAdult * mAmountAdult + mPriceFlightAdult * mPriceKidsPercentage * mAmountKids;
        mTextFlightPrice.setText(String.format("%.2f $", totalFlight));
        double totalHotel = mPriceHotelAdult * mAmountAdult + mPriceHotelAdult * mPriceKidsPercentage * mAmountKids;
        mTextHotelPrice.setText(String.format("%.2f $", totalHotel));
        mCurrentTotal = totalFlight + totalHotel + mTaxes;
        mTextTotalPrice.setText(String.format("%.2f $", mCurrentTotal));
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private class AsyncBooking extends AsyncTask<Void, Void, Integer> {

        String mCreditCardNumber;
        String mJourneyID;
        RestBooking mBooking;

        public AsyncBooking(String _creditCardNumber, String _journeyID){
            mCreditCardNumber = _creditCardNumber;
            mJourneyID = _journeyID;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            mBooking = new RestBooking(EasyTravelSettings.getServerHostName(getActivity()),
                    Integer.valueOf(EasyTravelSettings.getServerPort(getActivity())),
                    mJourneyID,
                    mApp.getLoggedInUser(),
                    mCreditCardNumber,
                    Double.toString(mCurrentTotal));
            int response = mBooking.doBooking();

            return response;
        }

        @Override
        protected void onPostExecute(Integer response) {
            super.onPostExecute(response);

            String bookingId = null;

            if (response == mBooking.BOOKING_SUCCESSFUL) {
                bookingId = mBooking.getBookingId();
            }

            mProgressDialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            if (bookingId != null) {
                builder.setTitle("Trip Booked Successfully!");
                builder.setMessage("Your trip to " + mTextNameJourney.getText().toString() + " has been booked. Price: $" + String.format("%.2f $", mCurrentTotal));
            }else{
                builder.setTitle("Trip not Booked!");
                builder.setMessage("Your trip to  " + mTextNameJourney.getText().toString() + " has NOT been booked.");
            }

            builder.setPositiveButton("ok", null);
            builder.show();

            if(EasyTravelSettings.shouldHaveErrorOnBookingAndSearch(getActivity())){
                try{
                    new Crash().pop();
                }catch (Exception ioobex){
                    // TODO: (4) report error
                }
            }
        }
    }
}
