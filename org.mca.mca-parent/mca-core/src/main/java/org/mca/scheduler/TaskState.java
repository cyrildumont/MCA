/**
 * 
 */
package org.mca.scheduler;


/**
 * @author Cyril
 *
 */
public enum TaskState {
	WAIT_FOR_COMPUTE,
	ON_COMPUTING,
	COMPUTED,
	RECOVERED,
	WAIT_FOR_ANOTHER_TASK,
	READY_TO_COMPUTE,
	ON_ERROR,
	UNKNOWN;
}

