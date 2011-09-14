package org.mca.skeleton;

import java.util.List;

import org.mca.deployer.ComputationCaseDeployer;
import org.mca.javaspace.exceptions.MCASpaceException;
import org.mca.math.DistributedVector;
import org.mca.scheduler.Task;

public abstract class SkeletonCaseDeployer extends ComputationCaseDeployer{


	protected <T,S> List<S> map(MapConfig config) throws MCASpaceException {
		MapAgent<T,S> agent = new MapAgent<T,S>(config) ;
		String name = config.getName();
		int nbPart = getNbPart(config.getInput());
		for(int i=1;i <= nbPart;i++){
			Task t = new Task(name + "-" + i);
			t.compute_agent_url = name;
			t.parameters=new Object[]{i} ;
			addTask(t);
		}
		return null;
	}

	private int getNbPart(String vectorName) throws MCASpaceException {
		DistributedVector<?> input = computationCase.<DistributedVector<?>>getData(vectorName);
		return input.getNbParts();
	}
	
}
