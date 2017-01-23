package ca.concordia.jtratch.sample;
import java.nio.BufferOverflowException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.rmi.AccessException;

import org.eclipse.core.runtime.CoreException;

class Person
{
    String name;

    public Person(String p_name) throws CoreException, AccessException, Exception
    {
        name = p_name;
        try
        {
            m1(); // possible exceptions: checked: CoreException, and _ from FileSystem getPath: InvalidPathException
            m2(); // possible exceptions: AccessViolationException, IOException
            m2(); /* possible exceptions: AccessViolationException, IOException*/ m3(-105); // possible exceptions: runtime: UnsupportedOperationException
//            m2(); // possible exceptions: AccessViolationException, IOException
//            PersonExtra.m30(); // pe: ---   none, it gets swallowed
//            FileSystem fileSystem = FileSystems.getDefault();
//            fileSystem.getPath("I'm toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo long for Windows File System to handle.");
            
        }
        catch (InvalidPathException ex)
        {
            //I'm the catch 22
        	
        	
        	
        	
        	System.out.println("someone tried to load a path which is too long!" + ex.getMessage());
        	try{		System.out.println("inner try: " + ex.getMessage());     	}
        	catch (Exception exp)        	{
        		System.out.println("inner catch" + ex.getMessage());       	}        	
        	
        }catch (BufferOverflowException ex)
        {
            //boffe morre aqui 1
        	 //boffe morre aqui 2
        	 //boffe morre aqui 3
        	 //boffe morre aqui 4
        } finally
        {
        	PersonExtra.m20();
        }
    }
    
    /**
     *
     * @throws  InvalidPathException
     *          If the path string cannot be converted
     * @throws  CoreException
     *           
     */
    public void m1() throws CoreException, Exception
    {
    	PersonExtra.m20(); // pe: CoreException
    	PersonExtra.m30(); //pe: -
        //s40 - system method that will throw something - InvalidPathException
        //pe: InvalidPathException
        FileSystem fileSystem = FileSystems.getDefault();
        fileSystem.getPath("I'm toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo long for Windows File System to handle.", new String[] {});
        
        //.get GetFullPath("I'm toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo long for Windows File System to handle.");
    }

    /// <exception cref="System.Runtime.InteropServices.COMException">Always throw & have a invalid xml char.</exception>
    private void m2() throws AccessException
    {
        try
        {
            //give me the ring! and
        	PersonExtra.m600();
        	throw new AccessException("tabloide"); // I will escape that catch! ha!
            
        }
        catch (InvalidPathException ex)
        {
        	System.out.println("mama, she called that long file again!");
            // I will tell mama if you do that again 
        }
    }
    private int m3(int x)
    {
        if (x > 0) { return x; };
        if (x < -100) { throw new UnsupportedOperationException(); };
        
        return m3(x + 1);
    }
}