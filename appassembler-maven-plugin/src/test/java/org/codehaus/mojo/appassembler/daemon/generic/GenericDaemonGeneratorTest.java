package org.codehaus.mojo.appassembler.daemon.generic;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.codehaus.mojo.appassembler.daemon.AbstractDaemonGeneratorTest;
import org.w3c.dom.Document;

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

//        File expectedAppXml = getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/generic/app.xml" );

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        builderFactory.setIgnoringComments( true );
        builderFactory.setIgnoringElementContentWhitespace( true );
        DocumentBuilder builder = builderFactory.newDocumentBuilder();

//        Document expected = builder.parse( expectedAppXml );
        Document actual = builder.parse( actualAppXml );
        
        assertNodeEquals( "com.westerngeco.example.App", "mainClass", actual );
        assertNodeEquals( "org/codehaus/mojo/appassembler/project-1/1.0-SNAPSHOT/project-1-1.0-SNAPSHOT.jar", "relativePath", actual );
        assertNodeEquals( "345", "initialMemorySize", actual );
        assertNodeEquals( "234", "maxMemorySize", actual );
        assertNodeEquals( "321", "maxStackSize", actual );
        assertNodeEquals( "foo=bar", "systemProperty", actual );
        assertNodeEquals( "arg1=arg1-value", "commandLineArgument", actual );

/* I'm too lazy to get this to work properly
        DaemonGeneratorService service = (DaemonGeneratorService) lookup( DaemonGeneratorService.ROLE );

        Daemon expectedDaemon = service.loadModel( expectedAppXml );
        Daemon actualDaemon = service.loadModel( actualAppXml );

        assertEquals( expectedDaemon, actualDaemon );
*/
    }

    private void assertNodeEquals( String expected, String tagName, Document document )
    {
        assertEquals( "Node with tag name " + tagName + " does not match", expected, document.getElementsByTagName( tagName ).item( 0 ).getFirstChild().getNodeValue() );
    }
}
