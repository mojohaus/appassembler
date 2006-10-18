package org.codehaus.mojo.appassembler.daemon;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JavaServiceWrapperDaemonGeneratorTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        JavaServiceWrapperDaemonGenerator daemonGenerator = (JavaServiceWrapperDaemonGenerator) lookup( DaemonGenerator.ROLE );
/*
        File basedir = getTestFile( "target/test-output/test-a" );

        FileUtils.deleteDirectory( basedir );

        Daemon daemon = new Daemon();

        daemon.setMainClass( "foo.bar.Main" );
        daemon.setName( "foo" );

        daemonGenerator.generate( basedir, daemon );

        // -----------------------------------------------------------------------
        // Assert
        // -----------------------------------------------------------------------

        File bin = new File( basedir, "bin" );
        File etc = new File( basedir, "etc" );

        File wrapper = new File( bin, "foo-wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile());
*/
    }
}
