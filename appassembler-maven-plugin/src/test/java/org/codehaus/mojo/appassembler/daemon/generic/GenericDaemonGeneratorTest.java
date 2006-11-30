package org.codehaus.mojo.appassembler.daemon.generic;

import org.codehaus.mojo.appassembler.daemon.AbstractDaemonGeneratorTest;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GenericDaemonGeneratorTest
    extends AbstractDaemonGeneratorTest
{
    public void testGenerationWithAllInfoInDescriptor()
        throws Exception
    {
        runTest( "generic", "src/test/resources/project-1/pom.xml", "src/test/resources/project-1/descriptor.xml", "target/output-1-generic" );

        File appXml = new File( getTestFile( "target/output-1-generic" ), "app.xml" );

        assertTrue( "config file is missing: " + appXml.getAbsolutePath(), appXml.isFile());

        assertTrue( FileUtils.contentEquals( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/generic/app.xml" ), appXml ));
    }
}
