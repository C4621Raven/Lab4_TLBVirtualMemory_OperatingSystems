public class PageTableEntry
{
	int phyiscalPageFrameNumber; //the index of the page in ram isFree array
	boolean inRAM; //true if page is in ram, false otherwise
	boolean recentlyModified;
	boolean recentlyReferenced;
	
	int pageTableIndex;
	
	PageTableEntry(int pageTableIndex)
	{
		this.pageTableIndex = pageTableIndex;
		//page size is 4096
		inRAM = false;
		recentlyModified = false;
		recentlyReferenced = false;
	}
}
