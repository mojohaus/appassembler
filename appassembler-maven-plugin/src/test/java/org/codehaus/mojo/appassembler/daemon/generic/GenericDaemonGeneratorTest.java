/**
 * The MIT License
 * 
 * Copyright 2006-2012 The Codehaus.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
        runTest( "generic", "src/test/resources/project-1/pom.xml", "src/test/resources/project-1/descriptor.xml",
                 "target/output-1-generic" );

        File actualAppXml = new File( getTestFile( "target/output-1-generic" ), "app.xml" );

        assertTrue( "config file is missing: " + actualAppXml.getAbsolutePath(), actualAppXml.isFile() );

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        builderFactory.setIgnoringComments( true );
        builderFactory.setIgnoringElementContentWhitespace( true );
        DocumentBuilder builder = builderFactory.newDocumentBuilder();

        Document actual = builder.parse( actualAppXml );

        assertNodeEquals( "com.westerngeco.example.App", "mainClass", actual );
        assertNodeEquals( "org/codehaus/mojo/appassembler/project-1/1.0-SNAPSHOT/project-1-1.0-SNAPSHOT.jar",
                          "relativePath", actual );
        assertNodeEquals( "345", "initialMemorySize", actual );
        assertNodeEquals( "234", "maxMemorySize", actual );
        assertNodeEquals( "321", "maxStackSize", actual );
        assertNodeEquals( "foo=bar", "systemProperty", actual );
        assertNodeEquals( "arg1=arg1-value", "commandLineArgument", actual );

    }

    //MAPPASM-143. The old unit tests testGenerationWithSnapshotDependencies() has been converted into
    //a integration tests to get the integration test running after a release change etc.
    private void assertNodeEquals( String expected, String tagName, Document document )
    {
        assertEquals( "Node with tag name " + tagName + " does not match", expected,
                      document.getElementsByTagName( tagName ).item( 0 ).getFirstChild().getNodeValue() );
    }

}
