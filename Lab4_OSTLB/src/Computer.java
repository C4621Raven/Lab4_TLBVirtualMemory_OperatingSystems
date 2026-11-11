import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Computer
{
	int numberOfMemoryAccesses;
	RAM ram;
	Disk disk;
	ArrayList<Task> arrayOfTasks;
	PageTable pt;
	
	Queue<PageTableEntry> q;
	
	int trackID;
	
	Computer(int ramSize, int diskSize) throws InvalidSize
	{
		numberOfMemoryAccesses = 0;
		
		if(ramSize%4096 != 0)
			throw new InvalidSize();
		if(diskSize%4096 != 0)
			throw new InvalidSize();
		if(diskSize < 10*ramSize)
			throw new InvalidSize();
		if(diskSize > 268435456)
			throw new InvalidSize();
		
		q = new LinkedList<PageTableEntry>(); 
		
		ram = new RAM(ramSize);
		disk = new Disk(diskSize);
		
		trackID = 0;
		
		arrayOfTasks = new ArrayList<Task>();
		pt = new PageTable();
	}
	
	public int allocateTask(int size)
	{
		trackID++;
		Task t = new Task(trackID);
		
		int newSize = (int)Math.ceil(size / 4096.00)*4096;
		
		try
		{
			t.locationOnDisk = disk.allocateSpace(newSize);
			t.startOfVirtualMemory = pt.allocateSpace(newSize/4096);
		}
		catch (InvalidSize | NoSpace e)
		{
			e.printStackTrace();
		}
		
		t.sizeOfData = newSize;
		
		t.computer = this;
		
		arrayOfTasks.add(t);
		
		for(int i = 0; i < newSize; i++)
		{
			pt.pt[t.startOfVirtualMemory+i] = new PageTableEntry(t.startOfVirtualMemory+i);
		}
		
		return trackID;
	}
	
	public void deallocateTask(int taskID)
	{
		Task t = getTask(taskID);
		try
		{
			disk.deallocateSpace(t.locationOnDisk, t.sizeOfData);
			pt.deallocateSpace(t.startOfVirtualMemory, t.sizeOfData);
		}
		catch (InvalidSize e)
		{
			e.printStackTrace();
		}
		arrayOfTasks.remove(t);
	}
	
	public Task getTask(int taskID)
	{
		for(Task t : arrayOfTasks)
		{
			if(t.taskID == taskID)
			{
				return t;
			}
		}
		return null;
	}
	
	/*
	 * returns the index in the RAM data array of the virtual address given the virtual address is the virtial address of a slot in the disk
	 */
	protected int loadVirtualPage(int virtualAddress, boolean willWrite, Task t) throws InvalidVirtualAddress
	{
		//0
		if(0 <= virtualAddress && virtualAddress < t.sizeOfData)
			throw new InvalidVirtualAddress();
		
		//1
		numberOfMemoryAccesses++;
		
		//2
		if(numberOfMemoryAccesses >= 100)
		{
			for(PageTableEntry pte : q)
			{
				pte.recentlyReferenced = false;
			}
			
			numberOfMemoryAccesses = 0;
		}
		
		//3
		int ptIndex = virtualAddress/4096 + t.startOfVirtualMemory; //index of the page in the pageTable where virtualAddress is in
		int pageVA = virtualAddress % 4096; //index of data where VA is relative to a page
		if(pt.pt[ptIndex].inRAM)
		{
			//6
			pt.pt[ptIndex].recentlyReferenced = true;
			if(willWrite)
				pt.pt[ptIndex].recentlyModified = true;
			
			//4
			return pt.pt[ptIndex].phyiscalPageFrameNumber * 4096 + pageVA;
		}
		
		//5a
		int diskStartIndex = ptIndex * 4096;
		for(int i = 0; i < ram.isFree.length; i++)
		{
			if(ram.isFree[i])
			{
				//i is the index of the page relative to isFree
				int ramDataStartIndex = i * 4096; //this is the index in RAM data array where the start of the page is
				pt.pt[ptIndex].phyiscalPageFrameNumber = i;
				
				q.add(pt.pt[ptIndex]);
				for(int j = 0; j < 4096; j++)
				{
					ram.data[ramDataStartIndex + j] = disk.data[diskStartIndex+j];
				}
				pt.pt[ptIndex].inRAM = true;
				
				//6
				pt.pt[ptIndex].recentlyReferenced = true;
				if(willWrite)
					pt.pt[ptIndex].recentlyModified = true;
				
				return ramDataStartIndex + pageVA;
			}
		}
		
		//5b
		//Enhanced Second Chance algorithm
		PageTableEntry kickPage = null;
		do
		{
			kickPage = q.poll();
			if(kickPage.recentlyReferenced)
			{
				kickPage.recentlyReferenced = false;
				q.add(kickPage);
			}
		}
		while(q.peek().recentlyReferenced);
		kickPage = q.poll();
		
		//write back to disk
		int ramDataStartIndex = kickPage.phyiscalPageFrameNumber * 4096; //starting index of the data in ram that will override
		if(kickPage.recentlyModified)
		{
			for(int i = 0; i < 4096; i++)
			{
				int diskDataKickStartIndex = kickPage.pageTableIndex * 4096; //starting index of the data in disk that will be overwritten
				disk.data[diskDataKickStartIndex + i] = ram.data[ramDataStartIndex + i];
			}
		}
		
		kickPage.inRAM = false;		
		
		pt.pt[ptIndex].phyiscalPageFrameNumber = kickPage.phyiscalPageFrameNumber;
		
		q.add(pt.pt[ptIndex]);
		for(int j = 0; j < 4096; j++)
		{
			ram.data[ramDataStartIndex + j] = disk.data[diskStartIndex+j];
		}
		pt.pt[ptIndex].inRAM = true;
		
		//6
		pt.pt[ptIndex].recentlyReferenced = true;
		if(willWrite)
			pt.pt[ptIndex].recentlyModified = true;
		
		return ramDataStartIndex + pageVA;
	}
	
	public void writeData(int virtualAddress, int dataToWrite, int taskID)
	{
		try
		{
			ram.data[loadVirtualPage(virtualAddress, true, getTask(taskID))] = dataToWrite;
		}
		catch (InvalidVirtualAddress e)
		{
			e.printStackTrace();
		}
	}
	
	public int readData(int virtualAddress, int taskID)
	{
		try
		{
			return ram.data[loadVirtualPage(virtualAddress, false, getTask(taskID))];
		}
		catch (InvalidVirtualAddress e)
		{
			e.printStackTrace();
		}
		return 0;
	}
}
