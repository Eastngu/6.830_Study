package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    private File f;
    private TupleDesc td;
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f=f;
        this.td=td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return this.f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        try{
            RandomAccessFile rfile = new RandomAccessFile(f,"r");
            int pagesize=BufferPool.getPageSize();
            byte[]buffer=new byte[pagesize];
            try {
                rfile.seek(pid.getPageNumber()*pagesize);
                if(rfile.read(buffer)==-1)
                    return null;
            }
            catch (IOException e){
                e.printStackTrace();
                return null;
            }
            rfile.close();
            return new HeapPage(new HeapPageId(pid.getTableId(),pid.getPageNumber()),buffer);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int)(this.f.length()/BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new DbFileIterator() {

            private int numPage = numPages();
            private int pid = 0;
            private BufferPool bufferPool = Database.getBufferPool();
            private HeapPage currPage;
            private Iterator<Tuple> currTupleIter;
            private boolean isOpen = false;

            @Override
            public void open() throws DbException, TransactionAbortedException {
                isOpen = true;
                getPage(pid++);
            }

            @Override
            public boolean hasNext() throws DbException, TransactionAbortedException {
                return isOpen && pid < numPage || (pid == numPage && currTupleIter.hasNext());
            }

            private boolean getPage(int pid) throws TransactionAbortedException, DbException {
                if (!isOpen) throw new DbException("not open");
                currPage = (HeapPage) bufferPool.getPage(tid, new HeapPageId(getId(), pid), Permissions.READ_ONLY);
                if (currPage == null) return false;
                currTupleIter = currPage.iterator();
                return true;
            }

            @Override
            public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
                if (!isOpen || currTupleIter == null)
                    throw new NoSuchElementException();
                if (!currTupleIter.hasNext()) {
                    getPage(pid++);
                }
                return currTupleIter.next();
            }

            @Override
            public void rewind() throws DbException, TransactionAbortedException {
                close();
                open();
            }

            @Override
            public void close() {
                pid = 0;
                isOpen = false;
                currPage = null;
                currTupleIter = null;
            }
        };
    }

}

