/**********************************************************************************
Copyright (c) 2010, Softfacade

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

* Neither the name of the Softfacade LLC nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

File: JRSessionData.java
Author: Gleb Burov — gleb.burov@softfacade.com gleb.burov@gmail.com
Company: Softfacade — hello@softfacade.com
Date: Wednesday, November 24, 2010
**********************************************************************************/

package com.softfacade.jrauthenticate;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import android.util.Log;

public class JRSessionData implements JRConnectionManagerDelegate {
	
	private JRSessionDelegate delegate;
	
	public JRProvider currentProvider;
	public JRProvider returningProvider;
	
	public Map<String, Map<String, String>> allProviders;
	private Map<String, Map<String, String>> providerInfo;
	public String[] configedProviders;
	
	private boolean hidePoweredBy;
	
	private String token;
	private String startURL;
	private String baseURL;
	
	private boolean forceReauth;
	
	private String errorStr;
	
	public JRSessionData(String URL, JRSessionDelegate del) throws Exception {

		if (URL == null || URL.length() == 0)
		{
			throw new Exception("URL is null or its length = 0");
		}
	
		delegate = del;
		baseURL = URL;
	
		currentProvider = null;
		returningProvider = null;
	
		allProviders = null;
		providerInfo = null;
		configedProviders = null;
	
		errorStr = null;
		forceReauth = false;
	
		startGetConfiguredProviders();
	}
	
	public String startURL() {
		Map<String, String> providerStats = (Map<String, String>) allProviders.get(currentProvider.getName());
		
		String oid;
		
		if (providerStats.get("openid_identifier") != null) {
			oid = "openid_identifier=" + providerStats.get("openid_identifier") + "&";
			
			if (currentProvider.getProviderRequiresInput()){
				oid.replaceAll("%@", currentProvider.getUserInput());
			} 
		} else {
			oid = "";
		}
		
		String str = baseURL + providerStats.get("url") + "?" + oid;
		if (forceReauth){
			str += "force_reauth=true&";
		}
		str += "device=iphone";
		
		forceReauth = false;
		return str;
		
	}
	
	private void loadCookieData() {
		// TODO Auto-generated method stub
	}

	
	public void setReturningProviderToProvider(JRProvider provider) {
		returningProvider = provider;
	}
	public void setProvider(String prov) {
		
		if (currentProvider == null || !currentProvider.getName().equals(prov)){
			
			if (returningProvider != null && returningProvider.getName().equals(prov)) {
				setCurrentProviderToReturningProvider();
			} else {
				try {
					currentProvider = new JRProvider(prov, allProviders.get(prov));
				} catch (Exception e) {
					Log.e("", e.getMessage());
				}
			}
			
		}
	}
	public void setCurrentProviderToReturningProvider() {
		currentProvider = returningProvider;
	}

	public void authenticationDidCancel() {
		delegate.jrAuthenticationDidCancel();
	}
	public void authenticationDidCompleteWithToken(String tok) {
		// TODO set cookie
		
		delegate.jrAuthenticationDidCompleteWithToken(tok, currentProvider.getName());
	}
	public void authenticationDidFailWithError(Error err) {
		delegate.jrAuthenticationDidFailWithError(err);
	}
	
	private void startGetConfiguredProviders() {
		//TODO: https
		String urlString = baseURL.replaceFirst("s", "") + "/openid/iphone_config";
		
		String tag = "getConfiguredProviders";
		
		HttpGet httpget = new HttpGet(urlString);
		httpget.setHeader("Accept", "application/json");
		httpget.setHeader("Content-type", "application/json");
		
		JRConnectionManager.getInstance().createConnectionFromRequest(httpget, this, tag);
		// Error handles in connectionDidFailWithError
	}
	
	private void finishGetConfiguredProviders(String dataStr) {
		
		JSONObject jObject = null;
		JSONTokener tokener = new JSONTokener(dataStr);
		try {
			jObject = new JSONObject(tokener);
			JSONArray jEnabledProviders = jObject.getJSONArray("enabled_providers");
			JSONObject jProviderInfo = jObject.getJSONObject("provider_info");
			int length = jEnabledProviders.length();
			configedProviders = new String[length];
			allProviders = new HashMap<String, Map<String,String>>();
			for(int i = 0; i < length; i++) {
				configedProviders[i] = jEnabledProviders.getString(i);
				JSONObject jProviderMap = jProviderInfo.getJSONObject(configedProviders[i]);
				JSONArray jProviderMapNames = jProviderMap.names();
				Map<String, String> providerMap = new HashMap<String, String>();
				for(int j = 0; j < jProviderMapNames.length(); j++) {
					providerMap.put(jProviderMapNames.getString(j), jProviderMap.getString(jProviderMapNames.getString(j)));
				}
				allProviders.put(configedProviders[i], providerMap);
			}
		} catch (JSONException e) {
			Log.e("", e.getMessage());
		}
		
		//TODO: set hidePoweredBy, loadCookieData()
	}
	
	@Override
	public void connectionDidFinishLoadingWithPayload(String payload,
			HttpRequest request, HttpClient client, String tag) {
		
		if (tag.equals("getConfiguredProviders")) {
			
			if (payload.indexOf("\"provider_info\":{") != -1)
			{
				finishGetConfiguredProviders(payload);
			}
			else {
				errorStr = "There was an error initializing JRAuthenticate.\nThere was an error in the response to a request.";
			}
		} 
	}
	
	@Override
	public void connectionDidFailWithError(Error error, HttpRequest request,
			String tag) {
		
		if (tag.equals("getBaseURL")) {
			errorStr = "There was an error initializing JRAuthenticate.\nThere was an error in the response to a request.";
		} else if (tag.equals("getConfiguredProviders")) {
			errorStr = "There was an error initializing JRAuthenticate.\nThere was an error in the response to a request.";
		}
	}

	@Override
	public void connectionWasStoppedWithTag(String tag) {
		
	}
	
	public final class JRProvider {
		private String name;
		private String friendlyName;
		private String placeholderText;
		private String shortText;
		private boolean providerRequiresInput;
		
		private String userInput;
		private String welcomeString;
		
		public Map providerStats;
		
		public JRProvider(String nm, Map stats) throws Exception {
			if (nm == null || nm.length() == 0 || stats == null)
			{
				throw new Exception("stats or nm is null or its length = 0");
			}

			providerStats = stats;
			setName(nm);

			setWelcomeString(null);

			placeholderText = null;
			shortText = null;
			setUserInput(null);
			friendlyName = null;
			providerRequiresInput = false;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}


		public String getFriendlyName() {
			return (String) providerStats.get("friendly_name");
		}

		public String getPlaceholderText() {
			return (String) providerStats.get("input_prompt");
		}

		public String getShortText() {
			String result = "";
			if (providerRequiresInput)
			{
				String[] arr = placeholderText.split(" ");
				if (arr.length > 4){
					for (int i = 2; i < arr.length - 2; i++){
						result += arr[i] + " ";
					}
				}
			}
			return result;
		}

		public void setUserInput(String userInput) {
			this.userInput = userInput;
		}

		public String getUserInput() {
			return userInput;
		}

		public void setWelcomeString(String welcomeString) {
			this.welcomeString = welcomeString;
		}

		public String getWelcomeString() {
			return welcomeString;
		}
		
		public boolean getProviderRequiresInput() {
			if (((String)providerStats.get("requires_input")).equals("YES")){
				return true;
			}
			return false;
		}

	}
}
