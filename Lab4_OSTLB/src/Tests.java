import static org.junit.Assert.*;

import org.junit.Test;

public class Tests
{
	//the following 4 tests double as tests for the PageTable class since the code is just copy pasted
	@Test
	public void allocationAndDeallocation()
	{
		try
		{
			Disk d = new Disk(4096*5);
			if(d.freeSpace.get(0).startValue != 0)
				fail("free spac to set up properly");
			if(d.freeSpace.get(0).size != 4096*5)
				fail("free spac to set up properly");
			
			d.allocateSpace(4096*4);
			if(d.freeSpace.get(0).startValue != 4096*4)
				fail("allocation not working");
			if(d.freeSpace.get(0).size != 4096)
				fail("allocation not working");
			
			d.deallocateSpace(0, 4096);
			if(d.freeSpace.get(0).startValue != 0)
				fail("allocation not working");
			if(d.freeSpace.get(0).size != 4096)
				fail("allocation not working");
		}
		catch (InvalidSize | NoSpace e)
		{
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void mergingLeft()
	{
		Disk d;
		try {
			d = new Disk(4096*5);
			d.allocateSpace(4096*4);
			d.deallocateSpace(0, 4096); //[1,0,0,0,1] each element is 4096 and 1 means free
			
			d.deallocateSpace(4096, 4096*2); //[1,1,1,0,1]
			if(d.freeSpace.get(0).startValue != 0)
				fail("left merging not working");
			if(d.freeSpace.get(0).size != 4096*3)
				fail("left merging not working");
			if(d.freeSpace.size() != 2)
				fail("left merging not working");
		}
		catch (InvalidSize | NoSpace e)
		{
			e.printStackTrace();
		}
	}
	
	@Test
	public void mergingRight()
	{
		Disk d;
		try {
			d = new Disk(4096*5);
			d.allocateSpace(4096*4);
			d.deallocateSpace(0, 4096); //[1,0,0,0,1] each element is 4096 and 1 means free
			
			d.deallocateSpace(4096*2, 4096*2); //[1,0,1,1,1]
			if(d.freeSpace.get(1).startValue != 4096*2)
				fail("left merging not working");
			if(d.freeSpace.get(1).size != 4096*3)
				fail("left merging not working");
			if(d.freeSpace.size() != 2)
				fail("left merging not working");
		}
		catch (InvalidSize | NoSpace e)
		{
			e.printStackTrace();
		}
	}
	
	@Test
	public void mergingBoth()
	{
		Disk d;
		try {
			d = new Disk(4096*5);
			d.allocateSpace(4096*4);
			d.deallocateSpace(0, 4096); //[1,0,0,0,1] each element is 4096 and 1 means free
			
			d.deallocateSpace(4096, 4096*3); //[1,1,1,1,1]
			if(d.freeSpace.get(0).startValue != 0)
				fail("left merging not working");
			if(d.freeSpace.get(0).size != 4096*5)
				fail("left merging not working");
			if(d.freeSpace.size() != 1)
				fail("left merging not working");
		}
		catch (InvalidSize | NoSpace e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void createTask()
	{
		try {
			Computer c = new Computer(4096,4096*11);
		} catch (InvalidSize e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}