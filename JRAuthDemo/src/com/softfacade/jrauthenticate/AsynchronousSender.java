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

File: AsynchronousSender.java
Author: Gleb Burov — gleb.burov@softfacade.com gleb.burov@gmail.com
Company: Softfacade — hello@softfacade.com
Date: Wednesday, November 24, 2010
**********************************************************************************/

package com.softfacade.jrauthenticate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.Handler;

public class AsynchronousSender extends Thread {

	private HttpClient httpClient;
 
	private HttpRequest request;
	private Handler handler;
	private CallbackWrapper wrapper;
	private String tag;
 
	protected AsynchronousSender(HttpClient httpClient, HttpRequest request,
			Handler handler, CallbackWrapper wrapper, String tag) {
		this.httpClient = httpClient;
		this.request = request;
		this.handler = handler;
		this.wrapper = wrapper;
		this.tag = tag;
	}
 
	public void run() {
		try {
			final HttpResponse response;
			synchronized (httpClient) {
				response = getClient().execute((HttpUriRequest) request);
			}
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String responseString = reader.readLine();
			// process response
			wrapper.setResponse(responseString, request, httpClient, tag);
			handler.post(wrapper);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	private HttpClient getClient() {
		return httpClient;
	}

	
}
