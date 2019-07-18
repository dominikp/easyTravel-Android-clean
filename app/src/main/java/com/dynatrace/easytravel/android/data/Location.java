/**
 * 
 */
package com.dynatrace.easytravel.android.data;

/**
 * @author pfhazw0
 *
 */
public class Location {

	private String name = "";
	private String created = "";
	
	public Location(String locationName, String locationCreated) {
		name = locationName;
		created = locationCreated;
	}

	public String getCreated() {
		return created;
	}

	public CharSequence getName() {
		return name;
	}

}
