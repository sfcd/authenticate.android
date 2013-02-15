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

File: JRWebViewActivity.java
Author: Gleb Burov � gleb.burov@softfacade.com gleb.burov@gmail.com
Company: Softfacade � hello@softfacade.com
Date: Wednesday, November 24, 2010
**********************************************************************************/

package com.softfacade.jrauthenticate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.sample.jrauthdemo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class JRWebViewActivity extends Activity  {
	
	private WebView mWebView;
	private JRSessionData sessionData;
	private JRAuthenticate jrAuth;
	
	public String startURL = null;
	public String token = null;
	public String device = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.jr_webview_activity);
		
		Intent intent = getIntent();
		String title = intent.getStringExtra("provider");
		setTitle(title);
		
		jrAuth = JRAuthenticate.jrAuthenticate();
		jrAuth.jrModalNavController.webView = this;
		jrAuth.jrModalNavController.whichIsActive = 0;
		sessionData = jrAuth.sessionData;
		
		mWebView = (WebView) findViewById(R.id.webview);
	    mWebView.getSettings().setJavaScriptEnabled(true);
	   	mWebView.addJavascriptInterface(new JRJavaScriptInterface(), "HTMLOUT");
	    
	    mWebView.setWebViewClient(new MyWebClient());
	    startURL = sessionData.startURL();
	    mWebView.loadUrl(startURL);
	    setProgressBarIndeterminateVisibility(true);
	}
	
	/* An instance of this class will be registered as a JavaScript interface */
	class JRJavaScriptInterface implements JRConnectionManagerDelegate
	{
	    public void showHTML(String html)
	    {
	    	int start = html.indexOf("name=\"token\"");
	    	if (start == -1) return;
	    	int tokenIndex = html.indexOf("value=\"", start);
	    	int end = html.indexOf("\">", tokenIndex);
	    	token = html.substring(tokenIndex+7, end);
	    	
	    	start = html.indexOf("name=\"device\"");
	    	int deviceIndex = html.indexOf("value=\"", start);
	    	end = html.indexOf("\">", deviceIndex);
	    	device = html.substring(deviceIndex+7, end);
	    	
	    	start = html.indexOf("action=\"");
	    	end = html.indexOf("\"", start+8);
	    	String url = html.substring(start+8, end);
	    	String tag = "rpx_result";
			
			HttpPost httppost = new HttpPost(url.replaceFirst("s", ""));
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("token", token));
			pairs.add(new BasicNameValuePair("device", device));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e) {
			}
							
			JRConnectionManager.getInstance().createConnectionFromRequest(httppost, this, tag);
			setProgressBarIndeterminateVisibility(true);
	    }
	    
	    @Override
		public void connectionDidFinishLoadingWithPayload(String payload,
				HttpRequest request, HttpClient client, String tag) {
	    	setProgressBarIndeterminateVisibility(false);
			if (tag.equals("rpx_result")) {
				
				JSONObject jObject = null;
				JSONTokener tokener = new JSONTokener(payload);
				try {
					jObject = new JSONObject(tokener);
					JSONObject jRpxResult = jObject.getJSONObject("rpx_result");
					
					if (jRpxResult.getString("stat").equals("ok")){
						sessionData.authenticationDidCompleteWithToken(jRpxResult.getString("token"));
					} else {
						String error = jRpxResult.getString("error");
						if (error.equals("Discovery failed for the OpenID you entered")) {
							String message = null;
							if (sessionData.currentProvider.getProviderRequiresInput())
								message = "The " + sessionData.currentProvider.getShortText() + " you entered was not valid. Please try again.";
							else
								message = "There was a problem authenticating with this provider. Please try again.";
							new AlertDialog.Builder(JRWebViewActivity.this)
				            .setTitle("Invalid Input")
				            .setMessage(message)
				            .setNegativeButton(android.R.string.ok, null)
				        .setCancelable(false)
				        .create()
				        .show();
						} else if (error.equals("Please enter your OpenID")) {
							Error err = new Error("Authentication failed: " + payload);
							sessionData.authenticationDidFailWithError(err);
						} else {
							Error err = new Error("Authentication failed: " + payload);
							sessionData.authenticationDidFailWithError(err);
						}
					}
				} catch (IllegalStateException e) {
					Log.e("", e.getMessage());
				} catch (JSONException e) {
					Log.e("", e.getMessage());
				}
			}
	    }


		@Override
		public void connectionDidFailWithError(Error error,
				HttpRequest request, String tag) {
			setProgressBarIndeterminateVisibility(false);
			if (tag.equals("rpx_result")) {
				sessionData.authenticationDidFailWithError(error);
			}
		}

		@Override
		public void connectionWasStoppedWithTag(String tag) {

		}
	}
	
	private class MyWebClient extends WebViewClient {
		
		/*@Override
		public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed() ;
		}*/
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			String thatURL = jrAuth.theBaseUrl + "/signin/device";
			if (url.indexOf(thatURL) != -1){
				return;
			}
			super.onPageStarted(view, url, favicon);
			setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			int index = startURL.indexOf("start");
			if (index != -1) {

                if (startURL.indexOf("connect_start") > -1)
                    index = startURL.indexOf("connect_start");

				String finishURL = startURL.substring(0, index);
				if (url.indexOf(finishURL) != -1) {
					view.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
					setProgressBarIndeterminateVisibility(true);
			    	return;
				}
			}
			setProgressBarIndeterminateVisibility(false);
		}

	}
}
