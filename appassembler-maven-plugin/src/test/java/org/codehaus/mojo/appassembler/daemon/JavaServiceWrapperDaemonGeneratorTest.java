package org.codehaus.mojo.appassembler.daemon;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JavaServiceWrapperDaemonGeneratorTest
    extends AbstractDaemonGeneratorTest
{

    protected String getGeneratorId()
    {
        return "jsw";
    }

    protected String getPOM()
    {
        return "src/test/resources/project-1/pom.xml";
    }

    protected String getDescriptor()
    {
        return "src/test/resources/project-1/descriptor.xml";
    }

    protected String getOutputDir()
    {
        return "target/output-1-jsw";
    }

    public void testBasic()
        throws Exception
    {
        super.testBasic();

        // -----------------------------------------------------------------------
        // Assert
        // -----------------------------------------------------------------------

        File bin = new File( basedir, "bin" );
        File etc = new File( basedir, "etc" );

        File wrapper = new File( getTestFile( getOutputDir() ), "etc/app-wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile());
    }
}
