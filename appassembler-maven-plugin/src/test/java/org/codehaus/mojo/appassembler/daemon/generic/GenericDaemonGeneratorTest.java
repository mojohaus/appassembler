package org.codehaus.mojo.appassembler.daemon.generic;

import org.codehaus.mojo.appassembler.daemon.AbstractDaemonGeneratorTest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService;
import org.codehaus.mojo.appassembler.model.Daemon;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GenericDaemonGeneratorTest
    extends AbstractDaemonGeneratorTest
{
    public void testGenerationWithAllInfoInDescriptor()
        throws Exception
    {
        runTest( "generic", "src/test/resources/project-1/pom.xml", "src/test/resources/project-1/descriptor.xml", "target/output-1-generic" );

        File actualAppXml = new File( getTestFile( "target/output-1-generic" ), "app.xml" );

        assertTrue( "config file is missing: " + actualAppXml.getAbsolutePath(), actualAppXml.isFile());

        File expectedAppXml = getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/generic/app.xml" );

/*      isEqualNode does not exist in 1.4 :(
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        builderFactory.setIgnoringComments( true );
        builderFactory.setIgnoringElementContentWhitespace( true );
        DocumentBuilder builder = builderFactory.newDocumentBuilder();

        Document expected = builder.parse( expectedAppXml );
        Document actual = builder.parse( actualAppXml );

        boolean equal = expected.isEqualNode( actual );
        assertTrue( "XML documents are not equal.", equal );
*/
/* I'm too lazy to get this to work properly
        DaemonGeneratorService service = (DaemonGeneratorService) lookup( DaemonGeneratorService.ROLE );

        Daemon expectedDaemon = service.loadModel( expectedAppXml );
        Daemon actualDaemon = service.loadModel( actualAppXml );

        assertEquals( expectedDaemon, actualDaemon );
*/
    }
}
