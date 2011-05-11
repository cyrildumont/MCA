package com.sun.jini.outrigger;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import net.jini.config.ConfigurationException;

import com.sun.jini.start.LifeCycle;

public class MCAPersistentOutriggerImpl extends MCAOutriggerServerWrapper{

	MCAPersistentOutriggerImpl(String[] configArgs, LifeCycle lifeCycle) 
	throws IOException, ConfigurationException, LoginException
	{
		super(configArgs, lifeCycle, true);
	}
	
}
