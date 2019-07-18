/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: RestCall.java
 * @date: Jun 26, 2014
 * @author: pfhazw0
 */
package com.dynatrace.easytravel.android.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import android.util.Log;

public abstract class RestCall {
	
	private static final String LOGTAG = RestCall.class.getSimpleName();
	
	protected String host;
	protected Integer port;
	protected int responseCode = -1;

	protected static final String SERVICES_PATH = "/services";
	protected static final String HTTP_POST = "POST";
	protected static final String RETURN = "return";

	protected static final String NAMESPACE_URL_BUSINESS_JPA = "http://business.jpa.easytravel.dynatrace.com/xsd";  //currently xmlns:ax212
	protected static final String NAMESPACE_URL_JPA = "http://jpa.easytravel.dynatrace.com/xsd";   //currently xmlns:ax213
	protected static final String NAMESPACE_URL_TRANSFEROBJ_WEBSERVICE_BUSINESS = "http://transferobj.webservice.business.easytravel.dynatrace.com/xsd";   //currently xmlns:ax216
	protected static final String NAMESPACE_URL_WEBSERVICE_BUSINESS = "http://webservice.business.easytravel.dynatrace.com";   //xmlns:ns
	protected static final String PREFIX_XMLNS = "xmlns:";

	protected static String nsBusinessJpaPrefix;
	protected static String nsJpaPrefix;
	protected static String nsTransferobjWebserviceBusinessPrefix;
	protected static String nsWebserviceBusinessPrefix;
	
	public RestCall(String requestHost, Integer requestPort) {
		host = requestHost;
		port = requestPort;
	}
	
	protected void doNetworkConnection() {
		
		URL url = buildUrl();
		
		String line;
		HttpURLConnection conn;
		BufferedReader input = null;
		StringBuilder response = new StringBuilder();
		
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(HTTP_POST);
			conn.setDoOutput(true);
			
			responseCode = conn.getResponseCode();

			input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			while ((line = input.readLine()) != null) {
				response.append(line);
			}

			input.close();
		} catch (IOException e) {
			Log.e(LOGTAG, "Unable to do network connection.", e);
		}

		String responseString = response.toString();
		//response example <ns:findLocationsResponse xmlns:ns="http://webservice.business.easytravel.dynatrace.com" xmlns:ax216="http://transferobj.webservice.business.easytravel.dynatrace.com/xsd" xmlns:ax212="http://business.jpa.easytravel.dynatrace.com/xsd" xmlns:ax213="http://jpa.easytravel.dynatrace.com/xsd"><ns:return ... returned data ... </ns:return></ns:findLocationsResponse>
		HashMap<String, String> namespacesMap = new HashMap<String, String>(8);
		if(responseString.indexOf("><") != -1){
			String namespaceString = responseString.substring(0, responseString.indexOf("><"));
			StringTokenizer tk1 = new StringTokenizer(namespaceString, " ");
			ArrayList<String> namespaceKV = new ArrayList<String>(8);
			while (tk1.hasMoreElements()) {
				namespaceKV.add(tk1.nextToken());
			}
			namespaceKV.remove(0);	//first element is no key=value mapping
			for (String kv : namespaceKV) {
				int index = kv.indexOf('=');
				String key = kv.substring(0, index);
				String value = kv.substring(index+2, kv.length()-1);	//remove leading and trailing "
				namespacesMap.put(key, value);
			}

			parseNamespacePrefix(namespacesMap);	//need to parse every time - e.g. if user changes easyTravel host while app is running
			parseXML(responseString);
		}
	}
	
	protected static String getParamString(HashMap<String, String> parameters) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("?");
		
		String prefix = "";

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			sb.append(prefix);
			prefix = "&";
			sb.append(URLEncoder.encode(entry.getKey()));
			sb.append("=");
			sb.append(URLEncoder.encode(entry.getValue()));
		}

		return sb.toString();
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	
	protected abstract void execute();
	protected abstract URL buildUrl();
	protected abstract void parseXML(String response);

	protected static void parseNamespacePrefix(Map<String, String> attributeMap) {
		//need to parse every time - e.g. if user changes easyTravel host while app is running
		for (Map.Entry entry : attributeMap.entrySet()) {
			String value = (String) entry.getValue();
			String key = (String) entry.getKey();
			if(value.equalsIgnoreCase(NAMESPACE_URL_BUSINESS_JPA)) {
				nsBusinessJpaPrefix = key.substring(PREFIX_XMLNS.length()) +":";
				Log.i(LOGTAG, "setting namspace prefix: " + nsBusinessJpaPrefix);
			} else if(value.equalsIgnoreCase(NAMESPACE_URL_JPA)) {
				nsJpaPrefix = key.substring(PREFIX_XMLNS.length()) +":";
				Log.i(LOGTAG, "setting namspace prefix: " + nsJpaPrefix);
			} else if(value.equalsIgnoreCase(NAMESPACE_URL_TRANSFEROBJ_WEBSERVICE_BUSINESS)) {
				nsTransferobjWebserviceBusinessPrefix = key.substring(PREFIX_XMLNS.length()) +":";
				Log.i(LOGTAG, "setting namspace prefix: " + nsTransferobjWebserviceBusinessPrefix);
			} else if(value.equalsIgnoreCase(NAMESPACE_URL_WEBSERVICE_BUSINESS)) {
				nsWebserviceBusinessPrefix = key.substring(PREFIX_XMLNS.length()) +":";
				Log.i(LOGTAG, "setting namspace prefix: " + nsWebserviceBusinessPrefix);
			}
		}
	}

	protected String nsReturn() {
		return nsWebserviceBusinessPrefix + RETURN;
	}
}
