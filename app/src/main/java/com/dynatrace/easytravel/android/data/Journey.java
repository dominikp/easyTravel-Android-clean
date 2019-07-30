package com.dynatrace.easytravel.android.data;

import com.dynatrace.easytravel.android.rest.RestJourney.JourneyRecord;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class Journey{

	private Location destination;
	private String fromDate;
	private String toDate;
	private Bitmap image;
	private double amount;
	private String description;
	private String name;
	private Location start;
	private String id;
	private String imgEnc;

	public Journey(Location destination, String fromDate, String toDate) {
		this.destination = destination;
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public Journey(JourneyRecord record) {
		
		destination = new Location(record.destination.name, record.destination.created);
		fromDate = record.fromDate;
		toDate = record.toDate;
		amount = record.amount;
		description = record.description;
		name = record.name;
		start = new Location(record.start.name, record.start.created);
		id = record.id;
		imgEnc = record.image;
		
		if (record.image != null) {
			byte[] blob = Base64.decode(record.image, 0);
			image = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		}
	}

	public Location getStart() {
		return start;
	}

	public String getName() {
		return name;
	}

	public double getAmount() {
		return amount;
	}

	public String getDescription() {
		return description;
	}

	public Bitmap getImage() {
		return image;
	}
	
	public String getImageEnc() {
		return imgEnc;
	}

	public Location getDestination() {
		return destination;
	}

	public String getFromDate() {
		return fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public String getId() {
		return id;
	}
}
