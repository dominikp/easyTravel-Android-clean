/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: RestJourney.java
 * @date: Jul 1, 2014
 * @author: pfhazw0
 */
package com.dynatrace.easytravel.android.rest;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class RestJourney extends RestCall {
	
	private static final String LOGTAG = RestJourney.class.getSimpleName();
	
	private boolean hasError = false;
	
	// REST parameters
	private static final String DESTINATION = "destination";
	private static final String FROM_DATE = "fromDate";
	private static final String TO_DATE = "toDate";
	
	// web services
	private static final String SEARCH_SERVICE = "JourneyService";
	private static final String JOURNEY_OPERATION = "findJourneys";
	
	private HashMap<String, String> params = new HashMap<String, String>();
	private ArrayList<JourneyRecord> searchRecords = new ArrayList<JourneyRecord>(20);

	// XML parsing
	private String nsCreated() {
		return nsJpaPrefix + "created";
	}

	private static String nsAmount() {
		return nsBusinessJpaPrefix + "amount";
	}

	private static String nsDescription() {
		return nsBusinessJpaPrefix + "description";
	}

	private static String nsDestination() {
		return nsBusinessJpaPrefix + DESTINATION;
	}

	private static String nsFromDate() {
		return nsBusinessJpaPrefix + FROM_DATE;
	}

	private static String nsFromDateTime() {
		return nsFromDate() + "Time";
	}

	private static String nsToDate() {
		return nsBusinessJpaPrefix + TO_DATE;
	}

	private static String nsId() {
		return nsBusinessJpaPrefix + "id";
	}

	private static String nsName() {
		return nsBusinessJpaPrefix + "name";
	}

	private static String nsPicture() {
		return nsBusinessJpaPrefix + "picture";
	}

	private static String nsStart() {
		return nsBusinessJpaPrefix + "start";
	}

	private static String nsTenant() {
		return nsBusinessJpaPrefix + "tenant";
	}

	public RestJourney(String requestHost, Integer requestPort, String query, Long from, Long to) {
		super(requestHost, requestPort);
		params.put(DESTINATION, query);
		params.put(FROM_DATE, Long.toString(from));
		params.put(TO_DATE, Long.toString(to));
	}
	
	public ArrayList<JourneyRecord> performSearch() {
		execute();
		return searchRecords;
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.easytravel.android.rest.RestCall#execute()
	 */
	@Override
	protected void execute() {
		doNetworkConnection();
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.easytravel.android.rest.RestCall#buildUrl()
	 */
	@Override
	protected URL buildUrl() {
		URL url = null;
		String urlString = String.format("%s:%s%s/%s/%s%s", host, port, SERVICES_PATH, SEARCH_SERVICE, JOURNEY_OPERATION, getParamString(params));

		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			Log.e(LOGTAG, "Malformed URL : " + e.toString());
		}
		
		return url;
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.easytravel.android.rest.RestCall#parseXML(java.lang.String)
	 */
	@Override
	protected void parseXML(String response) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document document = null;
		Element rootElement = null;
		
		InputSource is = new InputSource(new StringReader(response));
		
		try {
			builder = builderFactory.newDocumentBuilder();
			document = builder.parse(is);
			rootElement = document.getDocumentElement();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		if (rootElement == null) { // root element is not parsable - no response from server
			hasError = true;
			return;
		}

		NodeList nodelist = rootElement.getElementsByTagName(nsReturn());
		
		if (nodelist == null) { // no records found, searchRecords will be empty
			return;
		}

		for (int i = 0; i < nodelist.getLength(); i++) {
			if (nodelist.item(i) instanceof Element) {
				
				Element e = (Element) nodelist.item(i);
				NodeList l = e.getChildNodes();
				
				JourneyRecord record = new JourneyRecord();
				
				for (int j = 0; j < l.getLength(); j++) {

					Node thisNode = l.item(j);
					String thisName = l.item(j).getNodeName();
					
					if (thisName.equalsIgnoreCase(nsCreated())) {
						// don't need
					} else if (thisName.equalsIgnoreCase(nsTenant())) {
						// don't need
					} else if (thisName.equalsIgnoreCase(nsFromDateTime())) {
						// don't need
					} else if (thisName.equalsIgnoreCase(nsAmount())) {
						record.amount = parseDouble(thisNode.getTextContent());
					} else if (thisName.equalsIgnoreCase(nsDescription())) {
						record.description = thisNode.getTextContent();
					} else if (thisName.equalsIgnoreCase(nsDestination())) {
						record.destination = parseLocation(thisNode);
					} else if (thisName.equalsIgnoreCase(nsFromDate())) {
						record.fromDate = thisNode.getTextContent();
					} else if (thisName.equalsIgnoreCase(nsId())) {
						record.id = thisNode.getTextContent();
					} else if (thisName.equalsIgnoreCase(nsName())) {
						record.name = thisNode.getTextContent();
					} else if (thisName.equalsIgnoreCase(nsPicture())) {
						record.image = thisNode.getTextContent();
					} else if (thisName.equalsIgnoreCase(nsStart())) {
						record.start = parseLocation(thisNode);
					} else if (thisName.equalsIgnoreCase(nsToDate())) {
						record.toDate = thisNode.getTextContent();
					}
				}
				
				searchRecords.add(record);
			}
		}
	}
	
	public class JourneyRecord {
		public Location destination;
		public String fromDate;
		public String toDate;
		public String image;
		public double amount;
		public String description;
		public String name;
		public Location start;
		public String id;
	}
	
	public class Location {
		public String name;
		public String created;
		
		public Location(String locName, String locCreated) {
			name = locName;
			created = locCreated;
		}
	}
	
	private Location parseLocation(Node node) {
		NodeList list = node.getChildNodes();
		String created = "";
		String name = "";
		for (int i = 0; i < list.getLength(); i++) {
			String nodeName = list.item(i).getNodeName();
			if (nodeName.equalsIgnoreCase(nsCreated())) {
				created = list.item(i).getTextContent();
			} else if (nodeName.equalsIgnoreCase(nsName())) {
				name = list.item(i).getTextContent();
			}
		}
		
		return new Location(name, created);
	}
	
	private static Double parseDouble(String value) {
		Double doubleValue = 0D;
		try {
			doubleValue = Double.parseDouble(value);
		} catch (NumberFormatException nfe) {
			Log.d(LOGTAG, "Error parsing " + nsAmount() + " value " + value);
		}
		return doubleValue;
	}
	
	public boolean hasError() {
		return hasError;
	}
}
