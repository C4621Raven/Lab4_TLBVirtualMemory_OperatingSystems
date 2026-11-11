public class Task
{
	int taskID;
	int startOfVirtualMemory;
	int locationOnDisk;
	int sizeOfData;
	Computer computer;
	
	Task(int taskID)
	{
		this.taskID = taskID;
	}
	
	public int readData(int address)
	{
		return computer.readData(startOfVirtualMemory+address, taskID);
	}
	
	public void writeData(int address, int data)
	{
		computer.writeData(startOfVirtualMemory+address, data, taskID);
	}
}