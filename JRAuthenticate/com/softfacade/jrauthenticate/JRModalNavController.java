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

File: JRModalNavController.java
Author: Gleb Burov — gleb.burov@softfacade.com gleb.burov@gmail.com
Company: Softfacade — hello@softfacade.com
Date: Wednesday, November 24, 2010
**********************************************************************************/

package com.softfacade.jrauthenticate;

import android.app.Activity;

public class JRModalNavController {
	
	public Activity webView;
	public Activity userLandingView;
	public int whichIsActive = -1;
	
	public JRSessionData sessionData;
	
	JRModalNavController (JRSessionData sessionData) {
		this.sessionData = sessionData;
	}
	
	public void dismissModalNavigationController(boolean successfullyAuthed) {
		if (successfullyAuthed) {
			if (whichIsActive == 0) {
				webView.finish();
			} else if (whichIsActive == 1) {
				userLandingView.finish();
			}
		}
	}
	
}
