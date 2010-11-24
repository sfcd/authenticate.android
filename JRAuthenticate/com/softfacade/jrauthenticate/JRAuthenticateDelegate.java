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

File: JRAuthenticateDelegate.java
Author: Gleb Burov — gleb.burov@softfacade.com gleb.burov@gmail.com
Company: Softfacade — hello@softfacade.com
Date: Wednesday, November 24, 2010
**********************************************************************************/

package com.softfacade.jrauthenticate;

public interface JRAuthenticateDelegate {
	
	/**
	* These messages are both sent to any JRAuthenticateDelegates after the library has received the
	* token and before the library posts the token to the token URL. When this event occurs, the
	* library closes the modal dialog, so you may want to capture this moment so that you can update
	* you UI. You may implement neither or both, but the only difference between the two is that the
	* second one includes the provider the user is authenticating with (the first one was left for
	* backwards compatibility). If you instantiate the library with a token URL, authentication is
	* completed automatically, and you won't need the token for anything. As tokens are only valid for
	* a small amount of time, do not persist this value.
	*/
	void jrAuthenticate(JRAuthenticate jrAuth, String didReceiveToken);
	void jrAuthenticate(JRAuthenticate jrAuth, String didReceiveToken, String forProvider);
	/**
	* This message is sent to any JRAuthenticateDelegates after the library has posted the token to the token URL
	* and received a response from the token URL. This event completes authentication and the response is passed
	* along to the application in the tokenURLPayload. The content of this response is dependent on the implementation
	* of the token URL and application, but should contain any information required by the application, such as the
	* user's profile, session cookies, etc.
	*/
	void jrAuthenticateReachToken(JRAuthenticate jrAuth, String didReachTokenURL, String withPayload);
	/**
	* The following messages are sent when authentication failed (not canceled) for any reason.
	*/
	void jrAuthenticate(JRAuthenticate jrAuth, Error error);
	void jrAuthenticate(JRAuthenticate jrAuth, String callToTokenURL, Error didFailWithError);
	
	/**
	* This message is sent if the authorization was canceled for any reason other than an error. For example,
	* the user hits the "Cancel" button, or any class (including the JRAuthenticate delegate) calls the
	* cancelAuthentication message.
	*/
	void jrAuthenticateDidNotCompleteAuthentication(JRAuthenticate jrAuth);
}
