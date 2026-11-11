# Virtual Memory Manager

## Purpose of the Assignment

I completed this lab for my Operating Systems course to simulate how virtual memory works inside a computer. Programs believe they have a large, private block of memory to themselves, but the operating system actually keeps only some of that data in RAM and stores the rest on disk, moving pages back and forth as they are needed. I worked on my own simplified version of this system in Java to see how that translation and swapping happens. The simulation uses a single-level page table and leaves out the TLB, so the focus stays on paging and page replacement.

I designed a set of classes that work together like the parts of a real computer:

- **Page table entries**, which describe a single virtual page. Each entry stores the physical page frame it maps to, whether the page is currently in RAM, whether it has been recently referenced or modified, and its own position in the page table, which the computer uses to find where that page's data is stored on disk.
- **RAM**, which holds data in pages of 4096 values, along with an array marking which physical page frames are free. Its size must be a multiple of 4096, and it starts with every frame marked free.
- **Free space blocks**, which record the starting point and size of an unused region.
- **The disk**, which stores every task's full data and keeps a list of free space blocks. When space is requested, the disk takes it from the first free block in its list and shrinks or removes that block. It rejects sizes that aren't a multiple of 4096 and reports when no free space is left. When space is released, it is returned to the list and merged with any neighboring free blocks on either side to reduce fragmentation.
- **The page table**, an array of 268,435,456 page table entries, the size the assignment set for the virtual address space, with its own methods for allocating and freeing virtual space using free space blocks.
- **Tasks**, which represent running programs. Each task gets a unique ID that is never reused, a starting point in virtual memory, a location on disk, and a size, and has methods for reading and writing its data through the computer.
- **The computer**, which ties everything together. It is created with a RAM size and a disk size, and checks that both are multiples of 4096, that the disk is at least ten times the size of RAM, and that the disk is no larger than the virtual address space. It has methods for creating tasks, rounding their sizes up to a full page, and for removing them by releasing their space on the disk and in the page table. It also keeps a queue of the pages loaded into RAM, which it uses to choose a page to remove when RAM is full.

The core of the project is the method that handles each memory access. It is designed to:

- Check whether the address falls inside the task's own address space.
- Count the access, and after every 100 accesses clear the "recently referenced" flag on the pages in RAM so the system can tell which pages are still in active use.
- Split the address into a page number and an offset within that page, and return the page's location in RAM right away if it is already loaded.
- Otherwise, copy the page from disk into a free frame in RAM. If RAM is full, choose a page to remove using a second chance approach: pages are taken from the front of the queue, a page that was recently referenced has its flag cleared and is moved to the back, and a page that hasn't been used recently is removed. If that page was modified, its data is written back to disk before the new page takes its place.
- Mark the page as recently referenced, and as modified if the access was a write.

I created custom exceptions for invalid sizes, running out of space, and invalid virtual addresses. I also wrote JUnit tests for allocating and freeing disk space, including merging a freed block with the free space on its left, its right, and both sides at once, and for creating a computer.
