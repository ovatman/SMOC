package tr.edu.itu.cloudcorelab.pathpredictor.entity;

import java.util.ArrayList;

public class MemoryLog
{
	private ArrayList<Integer> memoryLogList;
	private int max;
	private int min;
	private int delta;
	
	public MemoryLog()
	{
		memoryLogList = new ArrayList<>();
		max = Integer.MIN_VALUE;
		min = Integer.MAX_VALUE;
	}
	
	public void store(int footprint)
	{
		memoryLogList.add(footprint);
		
		if (footprint > max)
		{
			max = footprint;
		}
		if (footprint < min)
		{
			min = footprint;
		}
		
		delta = max - min;
	}
	
	public int sizeOfMemoryLog()
	{
		return this.memoryLogList.size();
	}
	
	public int getMax()
	{
		return max;
	}
	
	public int getMin()
	{
		return min;
	}
	
	public int getDelta()
	{
		return delta;
	}
	
	public ArrayList<Integer> getMemoryLogList()
	{
		return memoryLogList;
	}
}
