package org.mca.util;

import java.util.Map;
import java.util.Set;

import etm.core.aggregation.ExecutionAggregate;
import etm.core.renderer.MeasurementRenderer;

public class MCARenderer implements MeasurementRenderer {

	@Override
	public void render(Map points) {
	
		Set<Map.Entry<String, ExecutionAggregate>> set =  points.entrySet();
		for (Map.Entry<String, ExecutionAggregate> point : set) {
			ExecutionAggregate ea = point.getValue();
			String name = ea.getName();
			double average = ea.getAverage();
			double min = ea.getMin();
			double max = ea.getMax();
			double total = ea.getTotal();
			long measurements = ea.getMeasurements();
			StringBuilder sb = new StringBuilder();
			sb.append(name);
			sb.append(";");
			sb.append(average);
			sb.append(";");
			sb.append(min);
			sb.append(";");
			sb.append(max);
			sb.append(";");
			sb.append(total);
			sb.append(";");
			sb.append(measurements);
			sb.append(";");
			System.out.println(sb.toString());
		}
	
	}

}
