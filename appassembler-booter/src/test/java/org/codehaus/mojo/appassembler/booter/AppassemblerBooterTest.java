package org.codehaus.mojo.appassembler.booter;

/*
 * The MIT License
 *
 * Copyright 2005-2007 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import junit.framework.TestCase;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class AppassemblerBooterTest
    extends TestCase
{
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // System.setProperty( "app.booter.debug", "true" );
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
            AppassemblerBooter.executeMain( classLoader, new String[0] );
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

    public void testLargeConfig()
        throws Exception
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
            if ( url.indexOf( "appassembler-booter/adgenerator-api-1.0-I11-SNAPSHOT.jar" ) >= 0 )
            {
                fail( "Broken path" );
            }
        }

    }

    public void testMainWithArgs()
        throws Throwable
    {
        System.setProperty( "app.name", "org/codehaus/mojo/appassembler/booter/appWithArgs" );

        URLClassLoader classLoader = AppassemblerBooter.setup();

        try
        {
            AppassemblerBooter.executeMain( classLoader, new String[]{"second argument"} );
        }
        catch ( InvocationTargetException e )
        {
            fail( "Unexpected failure: " + e );
        }

        classLoader = AppassemblerBooter.setup();

        try
        {
            AppassemblerBooter.executeMain( classLoader, new String[0] );
            fail( "Should have thrown exception" );
        }
        catch ( InvocationTargetException e )
        {
            assertTrue( "Unexpected exception " + e.getCause(),
                        e.getCause().getMessage().startsWith( "Expected two arguments" ) );
        }

    }
}
