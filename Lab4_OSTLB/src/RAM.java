public class RAM
{
	int[] data;
	boolean[] isFree;
	
	RAM(int size) throws InvalidSize
	{	
		data = new int[size];
		
		if(size%4096 != 0)
			throw new InvalidSize();
		isFree = new boolean[size/4096];
		
		for(int i = 0; i < size/4096; i++)
		{
			isFree[i] = true;
		}
	}
}