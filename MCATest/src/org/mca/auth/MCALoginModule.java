package org.mca.auth;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class MCALoginModule implements LoginModule{

	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map<String, ?> sharedState;
	private Map<String, ?> options;
	protected boolean committed = false;
	protected Principal principal = null;
	private String username;
	private String password;
	
    /** Status d'authentification : succeeded */
    private boolean succeeded = false;
    
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		System.out.println("initialize");
		this.subject = subject;
		this.callbackHandler = new MCACallBackHandler("admin","pass");
		this.sharedState = sharedState;
		this.options = options;
	}

	@Override
	public boolean login() throws LoginException {
		System.out.println("login");
		if (callbackHandler == null)
			throw new LoginException("No CallbackHandler specified");
		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("user name: ");
		callbacks[1] = new PasswordCallback("password: ", false);
		try {
			callbackHandler.handle(callbacks);
			username = ((NameCallback)callbacks[0]).getName();
			System.out.println("Username : " + username);
			char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
			password = new String(tmpPassword);
			System.out.println("Password : " + password);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new LoginException(ioe.toString());
		} catch (UnsupportedCallbackException uce) {
			uce.printStackTrace();
			throw new LoginException(uce.toString());
		}
		succeeded = "admin".equals(username) && "pass".equals(password);
		return succeeded;
	}

	@Override
	public boolean commit() throws LoginException {
		System.out.println("commit");
		if (!succeeded) {
		    return false;
		}else{
			return true;
		}
	}

	@Override
	public boolean abort() throws LoginException {
		System.out.println("abort");
		return false;
	}

	@Override
	public boolean logout() throws LoginException {
		System.out.println("logout");
		subject.getPrincipals().remove(principal);
		committed = false;
		principal = null;
		return (true);
	}


}
