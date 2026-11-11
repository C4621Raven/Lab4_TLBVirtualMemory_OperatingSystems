import java.util.ArrayList;

public class Disk
{
	int[] data;
	ArrayList<FreeSpace> freeSpace;
	
	Disk(int size) throws InvalidSize
	{
		if(size%4096 != 0)
			throw new InvalidSize();
		
		data = new int[size];
		
		freeSpace = new ArrayList<FreeSpace>();
		freeSpace.add(new FreeSpace(0,size));
	}
	
	public int allocateSpace(int size) throws InvalidSize, NoSpace
	{
		if(size%4096 != 0)
			throw new InvalidSize();
		
		if(freeSpace.size() == 0)
			throw new NoSpace();
		
		//we remove the size from the first FreeSpace and then update the start and size of the FreeSpace
		int out = freeSpace.get(0).startValue;
		if(size == freeSpace.get(0).size)
		{
			freeSpace.remove(0);
			return out;
		}
		freeSpace.get(0).startValue += size;
		freeSpace.get(0).size -= size;
		return out;
	}
	
	/**
	 * this only works if their is 0 overlap for between (index,size) and its surounding FreeSpaces
	 */
	public void deallocateSpace(int index, int size) throws InvalidSize
	{
		if(size%4096 != 0)
			throw new InvalidSize();
		
		freeSpace.add(new FreeSpace(index,size));
		mergeFreeSpace(index);
	}
	
	private void mergeFreeSpace(int index)
	{
		boolean removeLeft = false;
		boolean removeRight = false;
		
		freeSpace.sort((a,b)->{return a.startValue-b.startValue;});
		
		int indexInFS = -1;
		
		for(int i = 0; i < freeSpace.size(); i++)
		{
			if(freeSpace.get(i).startValue == index)
			{
				indexInFS = i;
			}
		}
		
		if(indexInFS>0)
		{
			int endIndexLeft = freeSpace.get(indexInFS-1).startValue + freeSpace.get(indexInFS-1).size - 1;
			if(freeSpace.get(indexInFS).startValue-1 == endIndexLeft)
			{
				removeLeft = true;
				freeSpace.get(indexInFS).startValue = freeSpace.get(indexInFS-1).startValue;
				freeSpace.get(indexInFS).size += freeSpace.get(indexInFS-1).size;
			}
		}
		
		if(indexInFS<freeSpace.size()-1)
		{
			int endIndex = freeSpace.get(indexInFS).startValue + freeSpace.get(indexInFS).size - 1;
			if(freeSpace.get(indexInFS+1).startValue-1 == endIndex)
			{
				removeRight = true;
				freeSpace.get(indexInFS).size += freeSpace.get(indexInFS+1).size;
			}
		}
		
		if(removeLeft && removeRight)
		{
			freeSpace.remove(indexInFS-1);
			freeSpace.remove(indexInFS);
			return;
		}
		
		if(removeLeft)
		{
			freeSpace.remove(indexInFS-1);
			return;
		}
		
		if(removeRight)
		{
			freeSpace.remove(indexInFS+1);
			return;
		}
	}
}