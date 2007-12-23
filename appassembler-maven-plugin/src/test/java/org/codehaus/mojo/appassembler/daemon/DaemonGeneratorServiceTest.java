package org.codehaus.mojo.appassembler.daemon;

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

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DaemonGeneratorServiceTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        DaemonGeneratorService generatorService = (DaemonGeneratorService) lookup( DaemonGeneratorService.ROLE );

        File stubDescriptor = getTestFile( "src/test/resources/model-1.xml" );
        assertTrue( stubDescriptor.canRead() );

        try
        {
            generatorService.loadModel( stubDescriptor );

            fail( "Expected exception." );
        }
        catch ( DaemonGeneratorException e )
        {
            assertTrue( e.getMessage().indexOf( "Missing required field from" ) > -1 );
            assertTrue( e.getMessage().indexOf( "main class" ) > -1 );
        }

        // -----------------------------------------------------------------------
        // Assert id generation
        // -----------------------------------------------------------------------

        Daemon daemon = generatorService.loadModel( getTestFile( "src/test/resources/model-2.xml" ) );

        assertEquals( "Foo", daemon.getMainClass() );
        assertNotNull( daemon.getId() );
        assertEquals( "foo", daemon.getId() );

        daemon = generatorService.loadModel( getTestFile( "src/test/resources/model-3.xml" ) );

        assertEquals( "foo.Bar", daemon.getMainClass() );
        assertNotNull( daemon.getId() );
        assertEquals( "bar", daemon.getId() );

        daemon = generatorService.loadModel( getTestFile( "src/test/resources/model-4.xml" ) );

        assertEquals( "foo.Bar", daemon.getMainClass() );
        assertNotNull( daemon.getId() );
        assertEquals( "jezloius", daemon.getId() );
    }

    public void testDaemonMerging()
        throws Exception
    {
        DaemonGeneratorService generatorService = (DaemonGeneratorService) lookup( DaemonGeneratorService.ROLE );

        Daemon dominant = new Daemon();
        Daemon recessive = new Daemon();

        assertNull( generatorService.mergeDaemons( null, null ) );

        assertEquals( dominant, generatorService.mergeDaemons( dominant, null ) );

        assertEquals( recessive, generatorService.mergeDaemons( null, recessive ) );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        recessive.setMainClass( "bar" );
        assertEquals( "bar", generatorService.mergeDaemons( dominant, recessive ).getMainClass() );
        dominant.setMainClass( "foo" );
        assertEquals( "foo", generatorService.mergeDaemons( dominant, recessive ).getMainClass() );

        dominant = new Daemon();
        recessive = new Daemon();
        recessive.setId( "bar" );
        assertEquals( "bar", generatorService.mergeDaemons( dominant, recessive ).getId() );
        dominant.setId( "foo" );
        assertEquals( "foo", generatorService.mergeDaemons( dominant, recessive ).getId() );

        dominant = new Daemon();
        recessive = new Daemon();
        assertEquals( 0, generatorService.mergeDaemons( dominant, recessive ).getCommandLineArguments().size() );

        recessive.getCommandLineArguments().add( "1" );
        recessive.getCommandLineArguments().add( "2" );
        recessive.getCommandLineArguments().add( "3" );
        assertEquals( 3, generatorService.mergeDaemons( dominant, recessive ).getCommandLineArguments().size() );

        dominant.getCommandLineArguments().add( "1" );
        dominant.getCommandLineArguments().add( "2" );
        assertEquals( 2, generatorService.mergeDaemons( dominant, recessive ).getCommandLineArguments().size() );

        // -----------------------------------------------------------------------
        // System properties
        // -----------------------------------------------------------------------

        dominant = new Daemon();
        recessive = new Daemon();
        assertNull( generatorService.mergeDaemons( dominant, recessive ).getJvmSettings() );

        JvmSettings settings = new JvmSettings();
        dominant.setJvmSettings( settings );
        assertEquals( settings, generatorService.mergeDaemons( dominant, recessive ).getJvmSettings() );
        recessive.setJvmSettings( settings );
        assertEquals( settings, generatorService.mergeDaemons( dominant, recessive ).getJvmSettings() );

        dominant = new Daemon();
        recessive = new Daemon();
        dominant.setJvmSettings( new JvmSettings() );
        recessive.setJvmSettings( new JvmSettings() );
        dominant.getJvmSettings().addSystemProperty( "1" );
        dominant.getJvmSettings().addSystemProperty( "2" );
        JvmSettings mergedSettings = generatorService.mergeDaemons( dominant, recessive ).getJvmSettings();
        assertEquals( 2, mergedSettings.getSystemProperties().size() );

        dominant = new Daemon();
        recessive = new Daemon();
        dominant.setJvmSettings( new JvmSettings() );
        recessive.setJvmSettings( new JvmSettings() );
        dominant.getJvmSettings().addSystemProperty( "1" );
        dominant.getJvmSettings().addSystemProperty( "2" );
        dominant.getJvmSettings().addSystemProperty( "3" );
        recessive.getJvmSettings().addSystemProperty( "a" );
        recessive.getJvmSettings().addSystemProperty( "b" );
        mergedSettings = generatorService.mergeDaemons( dominant, recessive ).getJvmSettings();
        assertEquals( 3, mergedSettings.getSystemProperties().size() );
        assertEquals( "1", mergedSettings.getSystemProperties().get( 0 ) );
        assertEquals( "2", mergedSettings.getSystemProperties().get( 1 ) );
        assertEquals( "3", mergedSettings.getSystemProperties().get( 2 ) );
    }
}
