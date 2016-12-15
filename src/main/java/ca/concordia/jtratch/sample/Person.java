package ca.concordia.jtratch.sample;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
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
            m1(); // possible exceptions: checked: CoreException, and _ from FileSystem getPath: InvalidPathException
            m2(); // possible exceptions: AccessViolationException, IOException
            m2(); // possible exceptions: AccessViolationException, IOException
            m3(-105); // possible exceptions: runtime: UnsupportedOperationException
            m2(); // possible exceptions: AccessViolationException, IOException
            m30(); // pe: ---   none, it gets swallowed
            FileSystem fileSystem = FileSystems.getDefault();
            fileSystem.getPath("I'm toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo long for Windows File System to handle.");
            
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
    
    /**
     * Converts a path string, or a sequence of strings that when joined form
     * a path string, to a {@code Path}. If {@code more} does not specify any
     * elements then the value of the {@code first} parameter is the path string
     * to convert. If {@code more} specifies one or more elements then each
     * non-empty string, including {@code first}, is considered to be a sequence
     * of name elements (see {@link Path}) and is joined to form a path string.
     * The details as to how the Strings are joined is provider specific but
     * typically they will be joined using the {@link #getSeparator
     * name-separator} as the separator. For example, if the name separator is
     * "{@code /}" and {@code getPath("/foo","bar","gus")} is invoked, then the
     * path string {@code "/foo/bar/gus"} is converted to a {@code Path}.
     * A {@code Path} representing an empty path is returned if {@code first}
     * is the empty string and {@code more} does not contain any non-empty
     * strings.
     *
     * <p> The parsing and conversion to a path object is inherently
     * implementation dependent. In the simplest case, the path string is rejected,
     * and {@link InvalidPathException} thrown, if the path string contains
     * characters that cannot be converted to characters that are <em>legal</em>
     * to the file store. For example, on UNIX systems, the NUL (&#92;u0000)
     * character is not allowed to be present in a path. An implementation may
     * choose to reject path strings that contain names that are longer than those
     * allowed by any file store, and where an implementation supports a complex
     * path syntax, it may choose to reject path strings that are <em>badly
     * formed</em>.
     *
     * <p> In the case of the default provider, path strings are parsed based
     * on the definition of paths at the platform or virtual file system level.
     * For example, an operating system may not allow specific characters to be
     * present in a file name, but a specific underlying file store may impose
     * different or additional restrictions on the set of legal
     * characters.
     *
     * <p> This method throws {@link InvalidPathException} when the path string
     * cannot be converted to a path. Where possible, and where applicable,
     * the exception is created with an {@link InvalidPathException#getIndex
     * index} value indicating the first position in the {@code path} parameter
     * that caused the path string to be rejected.
     *
     * @param   first
     *          the path string or initial part of the path string
     * @param   more
     *          additional strings to be joined to form the path string
     *
     * @return  the resulting {@code Path}
     *
     * @throws  InvalidPathException
     *          If the path string cannot be converted
     */
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