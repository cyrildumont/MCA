package org.mca.util;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import etm.core.aggregation.Aggregator;
import etm.core.metadata.AggregatorMetaData;
import etm.core.monitor.EtmMonitorContext;
import etm.core.monitor.EtmPoint;
import etm.core.renderer.MeasurementRenderer;

public class MCAAggregator implements Aggregator{

	private Aggregator delegate;
	private EtmMonitorContext ctx;

	private final static String SEPARATOR = "\t";
	
	Map<String,List<String>> collect = new HashMap<String, List<String>>();
	private long startTime;
	
	public MCAAggregator(Aggregator aAggregator) {
		delegate = aAggregator;
	}

	@Override
	public void add(EtmPoint point) {
		
		String name = point.getName();
		List<String> lines = null;
		if ((lines =collect.get(name)) == null) {
			lines = new ArrayList<String>();
			collect.put(name, lines);
		}
		
		
		double startPointTime = (point.getStartTimeMillis() - startTime);
		
		String line = startPointTime + SEPARATOR + point.getTransactionTime();
		lines.add(line);
		delegate.add(point);
	}

	public void flush() {
		delegate.flush();
	}

	public void reset() {
		delegate.reset();
	}

	public void reset(final String symbolicName) {
		delegate.reset(symbolicName);
	}

	public void render(MeasurementRenderer renderer) {
		delegate.render(renderer);
	}

	@Override
	public AggregatorMetaData getMetaData() {
		return null;
	}

	@Override
	public void init(EtmMonitorContext ctx) {
		this.ctx = ctx;
		delegate.init(ctx);
	}

	@Override
	public void start() {
		startTime = new Date().getTime();
	}

	@Override
	public void stop() {
		
	    delegate.stop();
	    try {
			
			for (Map.Entry<String,List<String>> lines : collect.entrySet()) {
				String name = lines.getKey();
				
				FileWriter fis = 
					new FileWriter(System.getProperty("mca.home") + "/work/" + MCAUtils.getIP() + "/" +name + ".txt");
				
				for (String line : lines.getValue()) {
					fis.write(line + "\n");
				}
				fis.flush();
				fis.close();
		    
			}} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
