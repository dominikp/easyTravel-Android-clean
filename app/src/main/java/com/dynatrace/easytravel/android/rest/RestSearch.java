/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: RestSearch.java
 * @date: Jun 30, 2014
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class RestSearch extends RestCall {
	
	private static final String LOGTAG = RestSearch.class.getSimpleName();
	
	ArrayList<SearchRecord> searchRecords = new ArrayList<SearchRecord>(MAX_RESULTS);
	
	// REST parameters
	private static final String CHECK_FOR_JOURNEYS = "checkForJourneys";
	private static final String NAME = "name";
	private static final String CREATED = "created";
	private static final String MAX_RESULT_SIZE = "maxResultSize";
	private static final int MAX_RESULTS = 20;
	
	// web services
	private static final String SEARCH_SERVICE = "JourneyService";
	private static final String SEARCH_OPERATION = "findLocations";
	
	// XML parsing
	private static final String WILDCARD = "*";
	
	private HashMap<String, String> params = new HashMap<String, String>();


	private static String nsCreated() {
		return nsJpaPrefix + CREATED;
	}
	private static String nsName() {
		return nsBusinessJpaPrefix + NAME;
	}

	/**
	 *
	 * @param requestHost
	 * @param requestPort
	 * @param name
	 * @param checkForJourneys
	 */
	public RestSearch(String requestHost, Integer requestPort, String name, Boolean checkForJourneys) {
		super(requestHost, requestPort);
		params.put(CHECK_FOR_JOURNEYS, Boolean.toString(checkForJourneys));
		params.put(NAME, name);
		params.put(MAX_RESULT_SIZE, Integer.toString(MAX_RESULTS));
	}
	
	public ArrayList<SearchRecord> performSearch() {
		execute();
		return searchRecords;
	}
	
	public void execute() {
		doNetworkConnection();
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.easytravel.android.rest.RestCall#buildUrl()
	 */
	@Override
	protected URL buildUrl() {
		URL url = null;
		String urlString = String.format("%s:%s%s/%s/%s%s", host, port, SERVICES_PATH, SEARCH_SERVICE, SEARCH_OPERATION, getParamString(params));

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
			return;
		}

		NodeList nodelist = rootElement.getElementsByTagName(nsReturn());
		
		if (nodelist == null) { // no records found, searchRecords will be empty
			return;
		}

		for (int i = 0; i < nodelist.getLength(); i++) {
			if (nodelist.item(i) instanceof Element) {
				
				String name = null;
				String created = null;
				
				Element e = (Element) nodelist.item(i);
				NodeList results = e.getElementsByTagName(WILDCARD);
				
				for (int j = 0; j < results.getLength(); j++) {

					if (results.item(j) instanceof Element) {
						Element l = (Element) results.item(j);
						String node = l.getNodeName();
						
						if (node != null) {
							if (node.equalsIgnoreCase(nsName())) {
								name = l.getTextContent(); 
							} else if (node.equalsIgnoreCase(nsCreated())) {
								created = l.getTextContent();
							}
						}
					}
				}

				if (name != null && created != null) {
					searchRecords.add(new SearchRecord(name, created));
				}
				
			}
		}
	}
	
	 public class SearchRecord {
		 
		 public String name;
		 public String created;
		 
		 public SearchRecord(String recordName, String createdDate) {
			 this.name = recordName;
			 this.created = createdDate;
		 }
	}
}
