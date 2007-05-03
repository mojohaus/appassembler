package org.codehaus.mojo.appassembler.booter;

import junit.framework.TestCase;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class AppassemblerBooterTest extends TestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();

        // System.setProperty( "app.booter.debug", "true" );
    }

    public void testSystemProperties() throws Exception
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

    public void testRun() throws Throwable
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

    public void testLargeConfig() throws Exception
    {
        System.setProperty( "app.name", "org/codehaus/mojo/appassembler/booter/largeApp" );
        System.setProperty( "app.booter.debug", "true" );
        URLClassLoader classLoader = AppassemblerBooter.setup();
        ArrayList urls = new ArrayList();
        urls.addAll( Arrays.asList( classLoader.getURLs() ) );
        Iterator iter = urls.iterator();
        while ( iter.hasNext() )
        {
            String url = ( (URL) iter.next() ).getPath();
            if ( url.contains( "appassembler-booter/adgenerator-api-1.0-I11-SNAPSHOT.jar" ) )
            {
                fail( "Broken path" );
            }
        }

    }
}
