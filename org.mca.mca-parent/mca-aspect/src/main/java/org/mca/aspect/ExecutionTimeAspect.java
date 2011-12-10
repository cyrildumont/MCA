package org.mca.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.mca.util.MCAAggregator;

import etm.core.aggregation.Aggregator;
import etm.core.aggregation.RootAggregator;
import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import etm.core.renderer.SimpleTextRenderer;

public @Aspect abstract class ExecutionTimeAspect {

	/*
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
									pointcut startMonitor(): 	execution(* org.mca.worker.ComputingWorker.TaskExecutor.run(..));
	 */

	
	
	public @Pointcut void methodMonitoring(){}
	public @Pointcut void startMonitor() {}

	private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();

	@Around(value="methodMonitoring()")
	public Object methodMonitoring(ProceedingJoinPoint joinPoint) throws Throwable {
		String className = joinPoint.getSignature().getDeclaringType().getName();
		String methodName = joinPoint.getSignature().getName();
		EtmPoint point = etmMonitor.createPoint(className + ":" + methodName);
		Object[] args = joinPoint.getArgs();
		Object o = joinPoint.proceed(args);
		point.collect();
		return o;
	}

	@Around(value="startMonitor()")
	public Object startMonitor(ProceedingJoinPoint joinPoint) throws Throwable{
		Aggregator aggregator = new MCAAggregator(new RootAggregator());
		BasicEtmConfigurator.configure(true, aggregator);
		etmMonitor.start();
		Object[] args = joinPoint.getArgs();
		Object o = joinPoint.proceed(args);
		etmMonitor.render(new SimpleTextRenderer());
		etmMonitor.stop();		
		return o;
	}
}