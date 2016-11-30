package ca.concordia.jtratch.test;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.rmi.AccessException;

import org.eclipse.core.runtime.CoreException;

class Person
{
    String name;

    public Person(String p_name) throws CoreException, AccessException
    {
        name = p_name;
        try
        {
            m1(); // possible exceptions: COMException, and _ from FileSystem getPath: InvalidPathException
            m2(); // possible exceptions: AccessViolationException, IOException
            m2(); // possible exceptions: AccessViolationException, IOException
            m3(-105); // possible exceptions: NotImplementedException
            m2(); // possible exceptions: AccessViolationException, IOException
            m30(); // pe: ---   none, it gets swallowed
        }
        catch (InvalidPathException ex)
        {
            //I'm the catch 22
        	System.out.println("someone tried to load a path which is too long!" + ex.getMessage());
        } finally
        {
            m20();
        }
    }
    public void m1() throws CoreException
    {
        m20(); // pe: CoreException
        m30(); //pe: -
        //s40 - system method that will throw something - InvalidPathException
        //pe: InvalidPathException
        FileSystem fileSystem = FileSystems.getDefault();
        fileSystem.getPath("I'm toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo long for Windows File System to handle.");
        
        //.get GetFullPath("I'm toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo long for Windows File System to handle.");
    }


    /// <exception cref="System.Runtime.InteropServices.COMException">Always throw & have a invalid xml char.</exception>
    public void m20() throws CoreException
    {
        m500();
        m600();
        throw new CoreException(null);
    }
    private void m500()
    {
        //cool 
    }
    private void m600()
    {
        //notcool();//cool 
        
    }        
    private void m30()
    {
        try
        {
            //give me the ring! and
            throw new ArithmeticException();
        }
        catch (Exception ex)
        {
            // don't tell mama anything
            throw ex;
        }            
    }
    private void m2() throws AccessException
    {
        try
        {
            //give me the ring! and
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