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

File: MainActivity.java
Author: Gleb Burov — gleb.burov@softfacade.com gleb.burov@gmail.com
Company: Softfacade — hello@softfacade.com
Date: Wednesday, November 24, 2010
**********************************************************************************/

package com.sample.jrauthdemo;


import com.softfacade.jrauthenticate.JRAuthenticate;
import com.softfacade.jrauthenticate.JRAuthenticateDelegate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, JRAuthenticateDelegate {
    
	private Button singInBtn;
	
	public boolean isActiveSession;
	public JRAuthenticate jrAuth;
	
	final String appId = "<Your Application ID>";
	final String tokenUrl = "<Your Token URL";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        singInBtn = (Button) findViewById(R.id.signInButton);
        singInBtn.setOnClickListener(this);
        
        jrAuth = new JRAuthenticate(appId, tokenUrl, this);
    }


	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.signInButton:
			if (!isActiveSession) {
				jrAuth.showJRAuthenticateDialog(this, this, "Sign in with...");
			}
			break;
		}
	}


	@Override
	public void jrAuthenticate(JRAuthenticate jrAuth, String didReceiveToken) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void jrAuthenticate(JRAuthenticate jrAuth, String didReceiveToken,
			String forProvider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void jrAuthenticateReachToken(JRAuthenticate jrAuth,
			String didReachTokenURL, String withPayload) {
		Toast.makeText(this, "Successfully signed in", Toast.LENGTH_LONG).show();
	}


	@Override
	public void jrAuthenticate(JRAuthenticate jrAuth, Error error) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void jrAuthenticate(JRAuthenticate jrAuth, String callToTokenURL,
			Error didFailWithError) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void jrAuthenticateDidNotCompleteAuthentication(JRAuthenticate jrAuth) {
		// TODO Auto-generated method stub
		
	}
}