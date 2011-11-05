package org.mca.ext.spmd.topology;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PlanTest {

	private Plan plan;
	
	@Parameters
	public static Collection<Plan[]> data(){
		return Arrays.asList(new Plan[][]{
				{new Plan(3,4,3)},
				{new Plan(1,4,3)},
				{new Plan(10,4,3)},
				{new Plan(12,4,3)}
		});
	}
	
	public PlanTest(Plan plan){
		this.plan = plan;
	}
	
	@Test
	public void testGetRight() {
		int[] result = new int[1];
		result[0] = plan.getRight();
		int[] expecteds = new int[]{Topology.NULL_VALUE};
		assertArrayEquals(expecteds, result);
	}

	@Test
	public void testGetLeft() {
		int[] result = new int[1];
		result[0] = plan.getLeft();
		int[] expecteds = new int[]{2};
		assertArrayEquals(expecteds, result);
	}

	@Test
	public void testGetDown() {
		int[] result = new int[1];
		result[0] = plan.getDown();
		int[] expecteds = new int[]{6};
		assertArrayEquals(expecteds, result);
	}

	@Test
	public void testGetUp() {
		int[] result = new int[1];
		result[0] = plan.getUp();
		int[] expecteds = new int[]{Topology.NULL_VALUE};
		assertArrayEquals(expecteds, result);
	}

}
