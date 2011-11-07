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

	private int rank;
	private Plan plan;
	
	@Parameters
	public static Collection<Integer[]> data(){
		return Arrays.asList(new Integer[][]{
				{1},{3}
		});
	}
	
	public PlanTest(int rank){
		this.rank = rank;
		this.plan = new Plan(4, 3);
	}
	
	@Test
	public void testGetRight() {
		int[] result = new int[1];
		result[0] = plan.getRight(rank);
		int[] expecteds = new int[]{Topology.NULL_VALUE};
		assertArrayEquals(expecteds, result);
	}

	@Test
	public void testGetLeft() {
		int[] result = new int[1];
		result[0] = plan.getLeft(rank);
		int[] expecteds = new int[]{2};
		assertArrayEquals(expecteds, result);
	}

	@Test
	public void testGetDown() {
		int[] result = new int[1];
		result[0] = plan.getDown(rank);
		int[] expecteds = new int[]{6};
		assertArrayEquals(expecteds, result);
	}

	@Test
	public void testGetUp() {
		int[] result = new int[1];
		result[0] = plan.getUp(rank);
		int[] expecteds = new int[]{Topology.NULL_VALUE};
		assertArrayEquals(expecteds, result);
	}

}
