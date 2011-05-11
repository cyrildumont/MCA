package org.mca.auth;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class MCACallBackHandler implements CallbackHandler {

	private String username;
	private String password;
	
	public MCACallBackHandler(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	
	@Override
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		  for (Callback callback : callbacks) {
			if (callback instanceof NameCallback) {
				((NameCallback)callback).setName(username);
			}else if(callback instanceof PasswordCallback){
				final char[] passwordcontents;
                if (password != null) {
                    passwordcontents = password.toCharArray();
                } else {
                    passwordcontents = new char[0];
                }
				((PasswordCallback)callback).setPassword(passwordcontents);
			}
		}
	}

}
