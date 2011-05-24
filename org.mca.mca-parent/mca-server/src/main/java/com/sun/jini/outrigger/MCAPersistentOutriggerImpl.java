package com.sun.jini.outrigger;

import java.io.IOException;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.security.auth.login.LoginException;

import net.jini.config.ConfigurationException;

import com.sun.jini.start.LifeCycle;

public class MCAPersistentOutriggerImpl 
					extends PersistentOutriggerImpl implements NotificationBroadcaster,MCAPersistentOutriggerImplMBean{

	protected NotificationBroadcasterSupport nbs;
	
	MCAPersistentOutriggerImpl(String[] configArgs, LifeCycle lifeCycle)
			throws IOException, ConfigurationException, LoginException {
		super(configArgs, lifeCycle);
		nbs = new NotificationBroadcasterSupport();
	}

	@Override
	public void addNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws IllegalArgumentException {
		nbs.addNotificationListener(listener, filter, handback);
	}

	@Override
	public void removeNotificationListener(NotificationListener listener)
			throws ListenerNotFoundException {
		nbs.removeNotificationListener(listener);
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return nbs.getNotificationInfo();
	}
	
}
