package org.mca.test;

import java.util.Map;

import org.mca.entry.ComputationCase;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.exceptions.NoJavaSpaceFoundException;
import org.mca.math.Matrix;
import org.mca.math.Vector;
import org.mca.scheduler.Task;
import org.mca.scheduler.TaskState;
import org.mca.transaction.Transaction;
import org.mca.transaction.TransactionManager;


public class Test2 {

	public static void main(String[] args) throws Exception {
		Matrix<Double> m = new Matrix<Double>(10, 10,4,5);
		m.getNeighborhood(1);
		m.set(5, 5, 4.0);
	}


	private static void test() throws Exception{
		MCASpace space = new MCASpace("localhost");
		ComputationCase cc = space.getCase("fft");
		//Task t = cc.getTask("fft1");
		//System.out.println(t);
		Vector v = cc.getData("fft", Vector.class);
		Map<Integer, String> m =  v.dataHandlers;
		for (Map.Entry<Integer, String> e : m.entrySet()) {
			System.out.println(e.getKey() + " --> " + e.getValue());
		}
	}

}
