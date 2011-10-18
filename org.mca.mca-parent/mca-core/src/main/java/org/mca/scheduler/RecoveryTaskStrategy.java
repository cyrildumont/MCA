package org.mca.scheduler;

import java.util.Collection;

import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;

public interface RecoveryTaskStrategy {

	/**
	 * 
	 * @return
	 * @throws MCASpaceException
	 */
	Collection<? extends Task<?>> recoverTasksToCompute(ComputationCase computationCase) throws MCASpaceException;

}
