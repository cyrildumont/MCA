import etm.core.monitor.EtmMonitor;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmPoint;
import etm.core.configuration.BasicEtmConfigurator;

aspect ExectutionTimeAspect {

  pointcut methodsOfInterest(): execution(* org.mca.count*(..))
	pointcut monitorTimeText():
		execution(void *.main(..));
	
	
  private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();

  Object around(): methodsOfInterest() {
		String className = thisJoinPoint.getSignature().getDeclaringType().getName();
		String methodName = thisJoinPoint.getSignature().getName();
		EtmPoint point = etmMonitor.createPoint(className + ":" + methodName);
		Object o = proceed();
		point.collect();
		return o;
  }
  
  	void around(): monitorTimeText(){
		
		BasicEtmConfigurator.configure(true);
		etmMonitor.start();
		proceed();
		etmMonitor.render(new etm.core.renderer.SimpleTextRenderer());
		etmMonitor.stop();		
		
	}
}