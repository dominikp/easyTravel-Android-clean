/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: RestLogin.java
 * @date: Jun 27, 2014
 * @author: pfhazw0
 */
package com.dynatrace.easytravel.android.rest;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
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

public class RestLogin extends RestCall {
	
	private static final String LOGTAG = RestLogin.class.getSimpleName();
	
	// REST parameters
	private static final String USERNAME = "userName";
	private static final String PASSWORD = "password";
	
	// web services
	private static final String AUTH_OPERATION = "authenticate";
	private static final String AUTH_SERVICE = "AuthenticationService";
	
	// XML parsing
	
	// class-specific fields
	protected boolean isSuccessful = false;
	
	private HashMap<String, String> params = new HashMap<String, String>();

	public RestLogin(String requestHost, Integer requestPort, String user, String pass) {
		super(requestHost, requestPort);
		params.put(PASSWORD, pass);
		params.put(USERNAME, user);
	}
	
	public void execute() {
		doNetworkConnection();
	}
	
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
			isSuccessful = false;
			return;
		}

		NodeList nodelist = rootElement.getElementsByTagName(nsReturn());
		
		if (nodelist == null) {
			isSuccessful = false;
			return;
		}

		for (int i = 0; i < nodelist.getLength(); i++) {
			if (nodelist.item(i) instanceof Element) {
				isSuccessful = Boolean.parseBoolean(nodelist.item(i).getTextContent());
			}
		}
	}

	protected URL buildUrl() {
		URL url = null;
		String urlString = String.format("%s:%s%s/%s/%s%s", host, port, SERVICES_PATH, AUTH_SERVICE, AUTH_OPERATION, getParamString(params));

		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			Log.e(LOGTAG, "Malformed URL : " + e.toString());
		}
		
		return url;
	}
	
	public boolean isSuccessful() {
		return isSuccessful;
	}
}
