package org.mca.test;

import java.security.PrivilegedExceptionAction;
import java.util.Scanner;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.server.ServerTransaction;
import net.jini.security.BasicProxyPreparer;
import net.jini.space.JavaSpace05;

import org.mca.entry.MCAProperty;
import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.mca.server.ComputationCaseImpl;
import org.mca.transaction.MCAParticipant;
import org.mca.transaction.Transaction;
import org.mca.transaction.TransactionManager;



public class Test {

	public static void main(String[] args) throws Exception {

		LoginContext loginContext = new LoginContext("org.mca.Master");
		loginContext.login();
		Subject.doAsPrivileged(loginContext.getSubject(), 	
				new PrivilegedExceptionAction() {
			@Override
			public Object run() throws Exception {
				test2();
				return null;
			}
		}, null);

	}


	private static void test2() throws Exception{
	
		try {
			LookupLocator ll = new LookupLocator("jini://localhost");
			ServiceRegistrar registrar = ll.getRegistrar();
			Class<?>[] classes = new Class<?>[]{MCASpace.class};
			ServiceTemplate template = new ServiceTemplate(null, classes,null);
			System.out.println(registrar.lookup(template));
			BasicProxyPreparer preparer = new BasicProxyPreparer(false, null);
			MCASpace space = (MCASpace)registrar.lookup(template);
			space = (MCASpace)preparer.prepareProxy(space);
			//JavaSpace05 space = (JavaSpace05)registrar.lookup(template);
			//space = (JavaSpace05)preparer.prepareProxy(space);
//		ComputationCase c = (ComputationCase)registrar.lookup(template);
			ComputationCaseImpl c = null;
			c = (ComputationCaseImpl)space.addCase("Jacobi2", "Test de Jacobi");
			//c.host = "localhost";
			//System.out.println(c.host);
			c.addProperty(new MCAProperty("testJ", "3"));
			Transaction t = TransactionManager.create("localhost",10000000);
			MCAParticipant participant = new MCAParticipant();
			((ServerTransaction)t.getTransaction()).join(participant, 5);
			Scanner clavier = new Scanner(System.in);
			System.out.println("Press a key to stop ...");
			c.stop();
//			clavier.next();
//			Task task = new Task();
//			task.name="toto";
//			task.state = TaskState.WAIT_FOR_COMPUTE;
//			//c.addTask(task);
//			System.out.println("Press a key to commit transaction..."); 
//			clavier.next();
//			t.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
