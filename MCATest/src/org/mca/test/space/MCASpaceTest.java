package org.mca.test.space;

import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mca.entry.ComputationCase;
import org.mca.entry.ComputationCaseState;
import org.mca.entry.MCAProperty;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.CaseNotFoundException;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;

public class MCASpaceTest {

	MCASpace space;
	
	@Before
	public void setUp(){
		space = new MCASpace("localhost");
	}
	

	@Test
	public void addTaskJacobi(){
		try {
			ComputationCase computationCase = space.getCase("Jacobi");
			Task task = new Task();
			task.name = "r4_1";
			task.computing_agent_name ="jacobi";
			task.state = TaskState.WAIT_FOR_COMPUTE;
			computationCase.addTask(task);
		} catch (MCASpaceException e) {
			e.printStackTrace();
		} catch (CaseNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void addProperty(){
		try {
			ComputationCase computationCase = space.getCase("Jacobi");
			MCAProperty property = new MCAProperty("p4_2", "COUCOU");
			computationCase.addProperty(property);
		} catch (MCASpaceException e) {
			e.printStackTrace();
		} catch (CaseNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	@Test
	public void addCase(){
		ComputationCase computationCase = new ComputationCase();
		computationCase.name = "Jacobi";
		computationCase.description = "Relaxation de Jacobi";
		computationCase.state = ComputationCaseState.STARTED;
		try {
			computationCase = space.addCase(computationCase);
			
			System.out.println(computationCase.transaction);
			computationCase.addProperty("MAXITER", "1000000");
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	@Ignore
	public void addCase2(){
		ComputationCase computationCase = new ComputationCase();
		computationCase.name = "RungeKutta2";
		computationCase.description = "Calcul de Test 2";
		computationCase.state = ComputationCaseState.STARTED;
		try {
			computationCase = space.addCase(computationCase);
			System.out.println(computationCase.transaction);
			computationCase.addProperty("test2", "123");
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getTask(){
		try {
			ComputationCase computationCase = space.getCase("RungeKutta");
			computationCase.getTask("r1_1");
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getCase(){
		try {
			ComputationCase computationCase = space.getCase("RungeKutta");
			System.out.println(computationCase.description);
			System.out.println(computationCase.transaction);
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void getPropertiesCase1(){
		try {
			ComputationCase computationCase = space.getCase("RungeKutta");
			Map<String, String> properties = computationCase.getProperties();
			System.out.println(properties.size());
		} catch (MCASpaceException e) {
			e.printStackTrace();
		}
	}
	
}
