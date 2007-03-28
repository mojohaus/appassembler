package org.codehaus.mojo.appassembler.daemon.script;

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScriptGeneratorTest
    extends PlexusTestCase
{
    private static final String PREFIX = "src/test/resources/org/codehaus/mojo/appassembler/daemon/script/";

    protected void setUp()
        throws Exception
    {
        super.setUp();

        LoggerManager loggerManager = (LoggerManager) lookup( LoggerManager.ROLE );

        loggerManager.setThreshold( Logger.LEVEL_DEBUG );
    }

    public void testNormalShellScriptGeneration()
        throws Exception
    {
        for ( Iterator it = Platform.getAllPlatforms().iterator(); it.hasNext(); )
        {
            testNormalShellScriptGeneration( (Platform) it.next() );
        }
    }

    private void testNormalShellScriptGeneration( Platform platform )
        throws Exception
    {
        ScriptGenerator generator = (ScriptGenerator) lookup( ScriptGenerator.ROLE );

        Daemon daemon = new Daemon();

        daemon.setId( "test" );
        daemon.setMainClass( "foo.Bar" );

        File outputDirectory = getTestFile( "target/test-output/normal-shell/" + platform.getName() );

        generator.createBinScript( platform.getName(), daemon, outputDirectory );

        File expectedFile = getTestFile( PREFIX + "expected-" + daemon.getId() + platform.getBinFileExtension() );
        File actualFile = new File( outputDirectory, "bin/" + daemon.getId() + platform.getBinFileExtension() );

        assertFilesEqual( expectedFile, actualFile );
    }

    private void assertFilesEqual( File expectedFile, File actualFile )
        throws Exception
    {
        Reader expectedFReader = null;
        Reader actualFReader = null;

        try
        {
            expectedFReader = new FileReader( expectedFile );
            BufferedReader expectedReader = new BufferedReader( expectedFReader );

            actualFReader = new FileReader( actualFile );
            BufferedReader actualReader = new BufferedReader( actualFReader );

            String expectedLine = expectedReader.readLine();
            String actualLine = actualReader.readLine();

            if ( expectedLine == null && actualLine != null )
            {
                fail( "Expected file was empty but the actual file wasn't." );
            }

            int lineNo = 1;

            while( expectedLine != null )
            {
                assertEquals( "Expected line no " + lineNo + " was not correct.", expectedLine, actualLine );

                expectedLine = expectedReader.readLine();

                actualLine = actualReader.readLine();

                lineNo++;
            }
        }
        finally
        {
            IOUtil.close( expectedFReader );
            IOUtil.close( actualFReader );
        }
    }
}
