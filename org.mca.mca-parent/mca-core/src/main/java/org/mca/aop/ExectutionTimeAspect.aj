
package org.mca.aop;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.OpenDataException;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.jmx.EtmMonitorMBean;
import etm.core.monitor.EtmMonitor;

public aspect ExectutionTimeAspect {

	private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();

	pointcut executionTime():
		execution(* org.mca.entry.*.*(..)) ||
		execution(* org.mca.agent.*.*(..)) ||
		execution(* org.mca.javaspace.*.*(..)) ||
		execution(* org.mca.worker.*.*(..)) ||
		execution(* org.mca.scheduler.*.*(..)) ||
		execution(* com.sun.jini.outrigger.MCAOutriggerServerImpl.*(..)) ||
		execution(* org.mca.master.*.*(..));

	pointcut monitorTimeJMX():
		execution(void org.mca.startup.MCA.start(..));

	pointcut monitorTimeText():
		execution(void org.mca.computation.ComputationCase.main(..)) || execution(void org.mca.entry.ScanDir.run(..));
	
	Object around(): executionTime() {
		String className = thisJoinPoint.getSignature().getDeclaringType().getName();
		String methodName = thisJoinPoint.getSignature().getName();
		etm.core.monitor.EtmPoint point = etmMonitor.createPoint(className + ":" + methodName);
		Object o = proceed();
		point.collect();
		return o;
	}

	void around(): monitorTimeJMX(){
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		BasicEtmConfigurator.configure(true);
		if (mBeanServer != null) {
			try {
				javax.management.ObjectName objectName = new javax.management.ObjectName("MCA:service=PerformanceMonitor");
				EtmMonitorMBean mbean = new EtmMonitorMBean(etmMonitor,"MCA");
				mBeanServer.registerMBean(mbean, objectName);
			} catch (OpenDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstanceAlreadyExistsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanRegistrationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotCompliantMBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		etmMonitor.start();
		proceed();
		etmMonitor.stop();
	}
	
	void around(): monitorTimeText(){
		BasicEtmConfigurator.configure(true);
		etmMonitor.start();
		proceed();
		etmMonitor.render(new etm.core.renderer.SimpleTextRenderer());
		etmMonitor.stop();
		
	}

}