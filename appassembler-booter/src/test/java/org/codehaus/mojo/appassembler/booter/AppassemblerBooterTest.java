package org.codehaus.mojo.appassembler.booter;

import junit.framework.TestCase;

import java.io.File;
import java.net.URLClassLoader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class AppassemblerBooterTest
    extends TestCase
{
    protected void setUp()
        throws Exception
    {
        super.setUp();

//        System.setProperty( "app.booter.debug", "true" );
    }

    public void testSystemProperties()
        throws Exception
    {
        System.setProperty( "app.name", "org/codehaus/mojo/appassembler/booter/app" );

        // When running from Maven this property will be set
        if ( System.getProperty( "basedir" ) == null )
        {
            System.setProperty( "basedir", new File( "" ).getAbsolutePath() );
        }

        AppassemblerBooter.setup();

        assertEquals( "System property bar is not set", "foo", System.getProperty( "bar" ) );

        assertEquals( "System property foo is not set", "bar", System.getProperty( "foo" ) );
    }

    public void testRun()
        throws Throwable
    {
        System.setProperty( "app.name", "org/codehaus/mojo/appassembler/booter/app" );

        URLClassLoader classLoader = AppassemblerBooter.setup();

        System.out.println( "classLoader = " + classLoader );

        try
        {
            AppassemblerBooter.executeMain( classLoader );
        }
        catch ( InvocationTargetException e )
        {
            throw e.getTargetException();
        }

        Class klass = classLoader.loadClass( "org.codehaus.mojo.appassembler.booter.DummyMain" );

        System.out.println( "klass = " + klass );

        Field field = klass.getField( "kickAss" );

        assertTrue( field.getBoolean( klass ) );
    }
}
