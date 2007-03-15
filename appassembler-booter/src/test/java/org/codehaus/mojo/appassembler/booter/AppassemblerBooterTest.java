package org.codehaus.mojo.appassembler.booter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.mojo.appassembler.model.io.DaemonModelUtil;

import junit.framework.TestCase;
import junit.framework.TestResult;

public class AppassemblerBooterTest extends TestCase
{
    public void testSystemProperties() throws Exception
    {
        Daemon config = getConfig();
        AppassemblerBooter.setSystemProperties( config );
        assertEquals( "System property bar is not set", "foo", System.getProperty( "bar" ) );
        assertEquals( "System property foo is not set", "bar", System.getProperty( "foo" ) );

        config.setJvmSettings( null );

        try
        {
            AppassemblerBooter.setSystemProperties( config );
        }
        catch ( Throwable e )
        {
            fail( "Was not able to call with null vmargs" );
        }
    }

    private Daemon getConfig() throws IOException, URISyntaxException
    {
        return DaemonModelUtil.loadModel( new File( this.getClass().getResource( "app.xml" ).toURI() ) );
    }

    public void testRun() throws Exception
    {
        System.setProperty( "app.name", "org/codehaus/mojo/appassembler/booter/app" );
      //  AppassemblerBooter.main( new String[0] );
    }
}
