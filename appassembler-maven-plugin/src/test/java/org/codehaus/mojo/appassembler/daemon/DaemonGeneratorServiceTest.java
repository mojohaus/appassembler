package org.codehaus.mojo.appassembler.daemon;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.mojo.appassembler.model.Daemon;

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

        try
        {
            generatorService.loadModel( getTestFile( "src/test/resources/model-1.xml" ) );

            fail( "Expected exception." );
        }
        catch ( DaemonGeneratorException e )
        {
            assertTrue( e.getMessage().contains( "Missing required field from" ) );
            assertTrue( e.getMessage().contains( "main class" ) );
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
    }
}
