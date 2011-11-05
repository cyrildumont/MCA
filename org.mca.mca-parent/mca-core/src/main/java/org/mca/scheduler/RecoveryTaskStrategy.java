package org.mca.scheduler;

import java.io.Serializable;
import java.util.Collection;

import org.mca.javaspace.ComputationCase;
import org.mca.javaspace.exceptions.MCASpaceException;

public interface RecoveryTaskStrategy extends Serializable{

	/**
	 * 
	 * @return
	 * @throws MCASpaceException
	 */
	Collection<? extends Task<?>> recoverTasksToCompute(ComputationCase computationCase) throws MCASpaceException;

}
