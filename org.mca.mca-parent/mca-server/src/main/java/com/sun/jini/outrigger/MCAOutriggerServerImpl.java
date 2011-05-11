package com.sun.jini.outrigger;

import java.io.IOException;
import java.rmi.activation.ActivationException;
import java.rmi.activation.ActivationID;

import javax.security.auth.login.LoginException;

import net.jini.config.ConfigurationException;

import com.sun.jini.start.LifeCycle;

public class MCAOutriggerServerImpl extends OutriggerServerImpl{

	MCAOutriggerServerImpl(ActivationID arg0, LifeCycle arg1, String[] arg2,
			boolean arg3, OutriggerServerWrapper arg4) throws IOException,
			ConfigurationException, LoginException, ActivationException {
		super(arg0, arg1, arg2, arg3, arg4);
		
	}

}
