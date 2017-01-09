package ca.concordia.jtratch.sample;

import java.nio.BufferOverflowException;

import org.eclipse.core.runtime.CoreException;

public class PersonExtra {
	public static void m20() throws CoreException
    {
        try
        {
        	m500();
        	m600();
            throw new CoreException(null);
        } catch(BufferOverflowException boex)
        {
        	//fix the buffer
        }        
    }
	public static void m500(){/*//cool*/}
    public static void m600(){throw new BufferOverflowException();}        
    public static void m30() throws Exception
    {
        try
        {
            m600();
            throw new ArithmeticException();
        }
        catch (Exception ex)
        {
            // don't tell mama anything
            throw ex;
        }            
    }
}
