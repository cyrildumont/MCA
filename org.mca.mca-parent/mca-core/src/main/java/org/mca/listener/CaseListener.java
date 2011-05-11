package org.mca.listener;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.mca.scheduler.Task;

public abstract class CaseListener implements NotificationListener {


	@Override
	public void handleNotification(Notification notification, Object handback) {
		CaseActionType type = CaseActionType.valueOf(notification.getType());
		Object o  = notification.getSource();
		switch (type) {
		case ADD_TASK:
			taskAdded((Task)o);
			break;
		case REMOVE_TASK:
			taskRemoved((Task)o);
			break;
		default:
			break;
		}
	}
	
	protected abstract void taskAdded(Task task);
	
	protected abstract void taskRemoved(Task task);
	
}
