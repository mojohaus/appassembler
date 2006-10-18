package org.codehaus.mojo.appassembler.daemon;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.mojo.appassembler.model.generic.Daemon;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DaemonGeneratorServiceTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        DaemonGeneratorService generatorService = (DaemonGeneratorService) super.lookup( DaemonGeneratorService.ROLE );

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
}
