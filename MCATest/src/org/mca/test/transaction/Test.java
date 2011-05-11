package org.mca.test.transaction;

import org.mca.entry.ComputationCase;
import org.mca.entry.ComputationCaseState;
import org.mca.javaspace.MCASpace;
import org.mca.javaspace.exceptions.MCASpaceException;

public class Test {

	public void start() {
		MCASpace space;
		space = new MCASpace("localhost");

		ComputationCase computationCase = new ComputationCase();
		computationCase.name = "RungeKutta";
		computationCase.description = "Calcul de Test";
		computationCase.state = ComputationCaseState.STARTED;
		System.out.println(Thread.currentThread().getContextClassLoader());
		try {

			space.addCase(computationCase);
		} catch (MCASpaceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
