package org.codehaus.mojo.appassembler.model.io;

/*
 * The MIT License
 *
 * Copyright 2005-2007 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import junit.framework.TestCase;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DaemonModelUtilTest
    extends TestCase
{
    private static final String PREFIX = "src/test/resources/org/codehaus/mojo/appassembler/model/io";

    public void testParsingModel1()
        throws Exception
    {
        Daemon daemon = DaemonModelUtil.loadModel( PlexusTestCase.getTestFile( PREFIX + "/model-1.xml" ) );

        doAsserts1( daemon );

        // -----------------------------------------------------------------------
        // Write out the model to a file
        // -----------------------------------------------------------------------

        File outputFile = PlexusTestCase.getTestFile( "target/model-1.xml" );

        DaemonModelUtil.storeModel( daemon, outputFile );

        // -----------------------------------------------------------------------
        // And load it again and make sure that the serializer does the job too
        // -----------------------------------------------------------------------

        daemon = DaemonModelUtil.loadModel( outputFile );

        doAsserts1( daemon );
    }

    public void testParsingModel2()
        throws Exception
    {
        Daemon daemon = DaemonModelUtil.loadModel( PlexusTestCase.getTestFile( PREFIX + "/model-2.xml" ) );

        doAsserts2( daemon );

        // -----------------------------------------------------------------------
        // Write out the model to a file
        // -----------------------------------------------------------------------

        File outputFile = PlexusTestCase.getTestFile( "target/model-2.xml" );

        DaemonModelUtil.storeModel( daemon, outputFile );

        // -----------------------------------------------------------------------
        // And load it again and make sure that the serializer does the job too
        // -----------------------------------------------------------------------

        daemon = DaemonModelUtil.loadModel( outputFile );

        doAsserts2( daemon );
    }

    private void doAsserts1( Daemon daemon )
    {
        assertNotNull( daemon );
        assertEquals( "app", daemon.getId() );
        assertEquals( "com.westerngeco.example.App", daemon.getMainClass() );
        assertNotNull( daemon.getClasspath() );
        assertEquals( 3, daemon.getClasspath().size() );
        assertEquals( Dependency.class, daemon.getClasspath().get( 0 ).getClass() );
        Dependency dependency = (Dependency) daemon.getClasspath().get( 0 );
        assertEquals( "org.codehaus.mojo.appassembler", dependency.getGroupId() );
        assertEquals( "project-1", dependency.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", dependency.getVersion() );
        assertEquals( "org/codehaus/mojo/appassembler/project-1/1.0-SNAPSHOT/project-1-1.0-SNAPSHOT.jar", dependency.getRelativePath() );
        dependency = (Dependency) daemon.getClasspath().get( 1 );
        assertEquals( "org.codehaus.plexus", dependency.getGroupId() );
        assertEquals( "plexus-utils", dependency.getArtifactId() );
        assertEquals( "1.1", dependency.getVersion() );
        assertEquals( "org/codehaus/plexus/plexus-utils/1.1/plexus-utils-1.1.jar", dependency.getRelativePath() );
        Directory directory = (Directory) daemon.getClasspath().get( 2 );
        assertEquals( "etc", directory.getRelativePath() );
        JvmSettings jvmSettings = daemon.getJvmSettings();
        assertNotNull( jvmSettings );
        assertEquals( "345", jvmSettings.getInitialMemorySize());
        assertEquals( "234", jvmSettings.getMaxMemorySize());
        assertEquals( "321", jvmSettings.getMaxStackSize());
        assertNotNull( jvmSettings.getSystemProperties() );
        assertEquals( "foo=bar", jvmSettings.getSystemProperties().get( 0 ) );
        assertEquals( "trygve=cool", jvmSettings.getSystemProperties().get( 1 ) );
        assertNotNull( daemon.getCommandLineArguments() ) ;
        assertEquals( "arg1=arg1-value", daemon.getCommandLineArguments().get( 0 ) );
        assertNotNull( jvmSettings.getExtraArguments() );
        assertEquals( "-da", jvmSettings.getExtraArguments().get( 0 ) );
        assertEquals( "-splash:imageOfKaare.png", jvmSettings.getExtraArguments().get( 1 ) );
    }

    private void doAsserts2( Daemon daemon )
    {
        assertNotNull( daemon );
        assertEquals( "app", daemon.getId() );
        assertEquals( "com.westerngeco.example.App", daemon.getMainClass() );
        assertNotNull( daemon.getClasspath() );
        assertEquals( 3, daemon.getClasspath().size() );
        assertEquals( Dependency.class, daemon.getClasspath().get( 0 ).getClass() );
        Dependency dependency = (Dependency) daemon.getClasspath().get( 0 );
        assertEquals( "org.codehaus.mojo.appassembler", dependency.getGroupId() );
        assertEquals( "project-1", dependency.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", dependency.getVersion() );
        assertEquals( "org/codehaus/mojo/appassembler/project-1/1.0-SNAPSHOT/project-1-1.0-SNAPSHOT.jar", dependency.getRelativePath() );
        dependency = (Dependency) daemon.getClasspath().get( 1 );
        assertEquals( "org.codehaus.plexus", dependency.getGroupId() );
        assertEquals( "plexus-utils", dependency.getArtifactId() );
        assertEquals( "1.1", dependency.getVersion() );
        assertEquals( "org/codehaus/plexus/plexus-utils/1.1/plexus-utils-1.1.jar", dependency.getRelativePath() );
        Directory directory = (Directory) daemon.getClasspath().get( 2 );
        assertEquals( "etc", directory.getRelativePath() );
        assertNull( daemon.getJvmSettings() );
    }
}
