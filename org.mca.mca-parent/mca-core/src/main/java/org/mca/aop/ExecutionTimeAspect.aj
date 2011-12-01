package org.mca.aop;

import etm.core.monitor.EtmMonitor;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmPoint;
import etm.core.configuration.BasicEtmConfigurator;
import etm.contrib.console.HttpConsoleServer;
import etm.core.aggregation.Aggregator;
import etm.core.aggregation.RootAggregator;
import org.mca.util.MCARenderer;
import org.mca.util.MCAAggregator;
import etm.core.renderer.SimpleTextRenderer;

aspect ExecutionTimeAspect {

	pointcut methodsOfInterest(): 	execution(* org.mca.example.jacobi.agent.JacobiAgent.compute(..)) ||
									execution(* org.mca.example.jacobi.agent.JacobiAgent.exchange(..)) ||
									execution(* org.mca.example.jacobi.agent.JacobiAgent.loadData(..)) ||
									execution(* org.mca.example.jacobi.agent.JacobiAgent.unloadData(..)) ||
									execution(* org.mca.example.jacobi.agent.JacobiAgent.internalCompute(..)) ||
									execution(* org.mca.example.jacobi.agent.JacobiAgent.borderCompute(..)) ||
									execution(* org.mca.example.jacobi.agent.JacobiAgent.sendBorders(..)) ||
									execution(* org.mca.ext.spmd.SPMDAgent.barrierNeighbor(..)) ||
									execution(* org.mca.ext.masterworker.MasterAgent.execute(..)) ||
									execution(* org.mca.example.jacobi.agent.JacobiAgent.program(..)) ||
									execution(* org.mca.example.pi.agent.PIWorkerAgent.execute(..));
	
	pointcut monitorTimeText(): 	execution(* org.mca.worker.ComputingWorker.TaskExecutor.run(..));
	
	
  private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();

  Object around(): methodsOfInterest() {
		String className = thisJoinPoint.getSignature().getDeclaringType().getName();
		String methodName = thisJoinPoint.getSignature().getName();
		EtmPoint point = etmMonitor.createPoint(className + ":" + methodName);
		Object o = proceed();
		point.collect();
		return o;
  }
  
  Object around(): monitorTimeText(){
		
		Aggregator aggregator = new MCAAggregator(new RootAggregator());
			BasicEtmConfigurator.configure(true, aggregator);
		etmMonitor.start();
		Object o = proceed();
		etmMonitor.render(new SimpleTextRenderer());
		etmMonitor.stop();		
		return o;
		
	}
}