package org.mca.test.agent;

import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mca.agent.TaskNotifierAgentImpl;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;


public class TaskNotifierAgentTest {

	public static TaskNotifierAgentImpl agent;
	
	@BeforeClass
	public static void setup(){
		Task task = new Task();
		task.setState(TaskState.WAIT_FOR_COMPUTE);
		task.name = "test";
		ArrayList<String> road = new ArrayList<String>();
		road.add("cyril-ubuntu:48728");
		road.add("cyril-ubuntu:55680");
		road.add("cyril-ubuntu:48728");
	

		try {
			agent = new TaskNotifierAgentImpl(road);
			agent.setTask(task);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
	
	
	@Test
	public void testGetTask() {
		try {
			Assert.assertNotNull(agent.getTask());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNext() {
		try {
			agent.next();
			agent.next();
			agent.next();
			agent.next();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testStart() {
		agent.start();
	}

	@Test
	public void testSetTask() {
		fail("Not yet implemented");
	}

}
