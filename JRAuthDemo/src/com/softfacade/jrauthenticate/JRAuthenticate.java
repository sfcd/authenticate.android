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

File: JRAuthenticate.java
Author: Gleb Burov — gleb.burov@softfacade.com gleb.burov@gmail.com
Company: Softfacade — hello@softfacade.com
Date: Wednesday, November 24, 2010
**********************************************************************************/

package com.softfacade.jrauthenticate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.sample.jrauthdemo.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JRAuthenticate implements JRConnectionManagerDelegate, JRSessionDelegate {
	
	private static JRAuthenticate singletonJRAuth;
	
	public String theAppId;
	/**
	* This is the base URL of your Engage application.
	*/
	public String theBaseUrl;
	/**
	* This is the token URL you supplied when you created the instance of the
	* JRAuthenticate library.
	*/
	public String theTokenUrl;
	/**
	* This is the token returned to the library from the Engage server once your
	* user authenticates with a provider. This token is used to retrieve the
	* profile data of your user from your token URL. It has a short lifetime,
	* so it is not recommended that you store this anywhere.
	*/
	public String theToken;
	/**
	* This is the data returned from your token URL after the library POSTS
	* the token and your token URL makes the call to the auth_info API. The
	* library will pass this back to your application, but the contents of
	* this are dependent on your token URL's implementation.
	*/
	public String theTokenUrlPayload;
	
	public JRSessionData sessionData;
	public JRModalNavController jrModalNavController;
	public ArrayList<JRAuthenticateDelegate> delegates;
	
	public String errString;
	
	/**
	* Once an instance of the JRAuthenticate library is created, this will return
	* that instance. Otherwise, it will return nil.
	*/
	public static JRAuthenticate jrAuthenticate() {
		return singletonJRAuth;
	}
	
	/**
	* Use this function to create an instance of the JRAuthenticate library.
	* Arguments:
	* appID: This is your 20-character application ID. It is required.
	* tokenURL: This is url where the library will automatically POST the token.
	* It is not required, but if you don't supply one, the library won't
	* automatically POST the token. You can manually post the token by
	* calling the makeCallToTokenUrl message described below.
	* delegate: This is the class that implements the JRAuthenticateDelegate protocol.
	*/
	public static JRAuthenticate jrAuthenticateWithAppID(String appId, String tokenUrl, JRAuthenticateDelegate delegate) {
		if(singletonJRAuth != null)
			return singletonJRAuth;

		if (appId == null)
			return null;
		
		return new JRAuthenticate(appId, tokenUrl, delegate);
	}
	
	public JRAuthenticate(String appId, String tokenUrl, JRAuthenticateDelegate delegate) {
		
		singletonJRAuth = this;
		
		delegates = new ArrayList<JRAuthenticateDelegate>();
		delegates.add(delegate);                             
		
		theAppId = appId;
		theTokenUrl = tokenUrl;
		startGetBaseUrl();
	}
	
	private void startGetBaseUrl() {
		
		String urlString = "http://rpxnow.com/jsapi/v3/base_url?appId=" + theAppId + "&skipXdReceiver=true";
		
		String tag = "getBaseURL";
		
		HttpGet httpget = new HttpGet(urlString);
		
		JRConnectionManager.getInstance().createConnectionFromRequest(httpget, this, tag);
		// Error handles in connectionDidFailWithError
	}
	
	private void finishGetBaseUrl(String dataStr){
		
		String[] arr = dataStr.split("\"");

		if(arr == null)
			return;

		theBaseUrl = arr[1];
		int length = theBaseUrl.length();
		char endChar = theBaseUrl.charAt(length - 1);
		if (endChar == '/') {
			theBaseUrl = theBaseUrl.substring(0, length - 1);
		}

		if (sessionData == null)
			try {
				sessionData = new JRSessionData(theBaseUrl, this);
			} catch (Exception e) {
				Log.e("", e.getMessage());
			}

		if (jrModalNavController != null)
			jrModalNavController.sessionData = sessionData;
	}

	@Override
	public void connectionDidFinishLoadingWithPayload(String payload,
			HttpRequest request, HttpClient client, String tag) {
		
		if (tag.equals("getBaseURL")){
			
			if (payload.indexOf("RPXNOW._base_cb(true") != -1){
				finishGetBaseUrl(payload);
			} else {
				errString = "There was an error initializing JRAuthenticate.\nThere was an error in the response to a request.";
			}
		} else if (tag.indexOf("token_url:") != -1) {
			
			theTokenUrlPayload = payload;
		
			for (JRAuthenticateDelegate delegate : delegates) {
				delegate.jrAuthenticateReachToken(this, theTokenUrl, theTokenUrlPayload);
			}
		}
	}
	
	@Override
	public void connectionDidFailWithError(Error error, HttpRequest request,
			String tag) {
		
		if (tag.equals("getBaseURL")) {
			errString = "There was an error initializing JRAuthenticate.\nThere was an error in the response to a request.";
			errString = "There was an error initializing JRAuthenticate.\nThere was a problem getting the base url.";
		} else if (tag.equals("token_url:")) {
			errString = "There was an error initializing JRAuthenticate.\nThere was an error in the response to a request.";
			errString = "Problem initializing connection to Token URL";
			for (JRAuthenticateDelegate delegate : delegates) {
				delegate.jrAuthenticate(this, theTokenUrl, error);
			}
		}
	}

	@Override
	public void connectionWasStoppedWithTag(String tag) {
		
	}
	
	/**
	* Use this function to begin authentication. The JRAuthenticate library will
	* pop up a modal dialog and take the user through the sign-in process.
	*/
	public void showJRAuthenticateDialog(final Context context, JRAuthenticateDelegate delegate, String title) {
		
		boolean isAlreadyExists = false;
		for (JRAuthenticateDelegate delegate1 : delegates) {
			if (delegate1 == delegate) {
				isAlreadyExists = true;
			}
		}
		
		if (!isAlreadyExists)
			delegates.add(delegate);
		
		if (theBaseUrl == null) {
			startGetBaseUrl();
			return;
		}
		
		if (sessionData == null) {
			return;
		}
		
		if (sessionData.configedProviders == null) {
			return;
		}
		
		if (jrModalNavController == null)
			jrModalNavController = new JRModalNavController(sessionData);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		
		builder.setAdapter(new AccountsAdapter(context, sessionData.configedProviders), 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						/* Let sessionData know which provider the user selected */
						String provider = sessionData.configedProviders[which];
						sessionData.setProvider(provider);
						
						if (sessionData.currentProvider.getProviderRequiresInput() || (sessionData.returningProvider != null && provider.equals(sessionData.returningProvider.getName()))) {
							
							
							//TODO: myUserLandingController
							
							
							
						} else {
							Intent intent = new Intent(context, JRWebViewActivity.class);
							intent.putExtra("provider", provider);
							((Activity) context).startActivityForResult(intent, 0);
						}
						
					}
				});
		builder.create().show();
	}
	public void unloadModalViewController() {
		
	}
	
	/**
	* Use these functions if you need to cancel authentication for any reason.
	*/
	public void cancelAuthentication() {
		for (JRAuthenticateDelegate delegate : delegates) {
			delegate.jrAuthenticateDidNotCompleteAuthentication(this);
		}
		jrModalNavController.dismissModalNavigationController(false);
	}
	public void cancelAuthenticationWithError(Error e){
		for (JRAuthenticateDelegate delegate : delegates) {
			delegate.jrAuthenticate(this, e);
		}
		jrModalNavController.dismissModalNavigationController(false);
	}

	/**
	* Use this function if you need to post the token to a token URL that is
	* different than the one you initiated the library with, or if you didn't
	* use a token URL when initiating the library.
	*/
	public void makeCallToTokenUrl(String tokenURL, String token){
		
		
		HttpPost httppost = new HttpPost(tokenURL);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", token));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(pairs));
		} catch (UnsupportedEncodingException e) {
		}
		String tag = "token_url:" + tokenURL;
						
		JRConnectionManager.getInstance().createConnectionFromRequest(httppost, this, tag);
		// Error handles in connectionDidFailWithError
	}
	
	private void makeCallToTokenUrlWithToken(String token) {
		makeCallToTokenUrl(theTokenUrl, token);
	}

	@Override
	public void jrAuthenticationDidCancel() {
		for (JRAuthenticateDelegate delegate : delegates) {
			delegate.jrAuthenticateDidNotCompleteAuthentication(this);
		}
		jrModalNavController.dismissModalNavigationController(false);
		
	}

	@Override
	public void jrAuthenticationDidCompleteWithToken(String tok, String prov) {
		theToken = tok;
		
		for (JRAuthenticateDelegate delegate : delegates) {
			delegate.jrAuthenticate(this, tok);
			delegate.jrAuthenticate(this, tok, prov);
		}
		
		jrModalNavController.dismissModalNavigationController(true);
		
		if (theTokenUrl != null)
			makeCallToTokenUrlWithToken(tok);
	}

	@Override
	public void jrAuthenticationDidFailWithError(Error err) {
		for (JRAuthenticateDelegate delegate : delegates) {
			delegate.jrAuthenticate(this, err);
		}
		jrModalNavController.dismissModalNavigationController(false);
	}

	public final class AccountsAdapter extends ArrayAdapter<String> {
		private Context context;
		private String[] configedProviders;
		private LayoutInflater mInflater;

		public AccountsAdapter(Context context, String[] configedProviders) {
			super(context, R.layout.select_account_item, configedProviders);
			
			this.context = context;
			this.configedProviders = configedProviders;
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemViewHolder itemViewHolder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.select_account_item, parent, false);
				itemViewHolder = new ItemViewHolder();
				itemViewHolder.tvLabel = (TextView) convertView.findViewById(R.id.text_item);
				itemViewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.icon_item);
				convertView.setTag(itemViewHolder);
			} else {
				itemViewHolder = (ItemViewHolder) convertView.getTag();
			}
			
			String provider = configedProviders[position];
			Map<String, String> provider_stats = sessionData.allProviders.get(provider);
			String friendly_name = provider_stats.get("friendly_name");
			
			//TODO: specify your package name
			int imageResId = context.getResources().getIdentifier("com.sample.jrauthdemo:drawable/jrauth_"+ provider +"_icon", null, null);
			
			itemViewHolder.tvLabel.setText(friendly_name);
			itemViewHolder.ivIcon.setImageResource(imageResId);
			

			return convertView;
		}

		private final class ItemViewHolder {
			public TextView tvLabel;
			public ImageView ivIcon;
		}

	}
	
}
