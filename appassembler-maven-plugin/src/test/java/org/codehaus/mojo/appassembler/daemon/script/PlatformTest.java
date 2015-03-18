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
package org.codehaus.mojo.appassembler.daemon.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.mojo.appassembler.model.Classpath;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PlatformTest
    extends TestCase
{
    public void testCreateClassPathUnix()
        throws Exception
    {
        test( Platform.getInstance( "unix" ), new String[] { "/foo/bar", "bar/foo", "/foo/bar:\"$BASEDIR\"/bar/foo",
            "group/artifact/version/artifact-version.jar",
            "/foo/bar:\"$BASEDIR\"/bar/foo:\"$REPO\"/group/artifact/version/artifact-version.jar", } );
    }

    public void testCreateClassPathWindows()
        throws Exception
    {
        test( Platform.getInstance( "windows" ), new String[] { "/foo/bar", "bar/foo",
            "\\foo\\bar;\"%BASEDIR%\"\\bar\\foo", "group/artifact/version/artifact-version.jar",
            "\\foo\\bar;\"%BASEDIR%\"\\bar\\foo;\"%REPO%\"\\group\\artifact\\version\\artifact-version.jar" } );
    }

    public void testGetAppArgumentsUnix()
        throws Exception
    {
        for ( Platform platform : Platform.getPlatformSet( Collections.singletonList( "all" ) ) )
        {
            testGetAppArguments( platform );
        }
    }

    private void testGetAppArguments( Platform util )
        throws Exception
    {
        Daemon daemon = new Daemon();
        assertEquals( null, util.getAppArguments( daemon ) );

        List<String> commandLineArguments = new ArrayList<String>();
        daemon.setCommandLineArguments( commandLineArguments );

        assertEquals( null, util.getAppArguments( daemon ) );

        commandLineArguments.add( "yo" );
        assertEquals( "yo", util.getAppArguments( daemon ) );

        commandLineArguments.add( "yo" );
        assertEquals( "yo yo", util.getAppArguments( daemon ) );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private void test( Platform util, String[] asserts )
        throws Exception
    {
        Daemon daemon = new Daemon();
        daemon.setClasspath( new Classpath() );

        assertEquals( "", util.getClassPath( daemon ) );

        List<Directory> classpathDirectories = daemon.getClasspath().getDirectories();
        classpathDirectories.add( createDirectory( asserts[0] ) );
        classpathDirectories.add( createDirectory( asserts[1] ) );
        assertEquals( asserts[2], util.getClassPath( daemon ) );

        List<Dependency> classpathDependencies = daemon.getClasspath().getDependencies();
        classpathDependencies.add( createDependency( asserts[3] ) );
        assertEquals( asserts[4], util.getClassPath( daemon ) );
    }

    private Dependency createDependency( String relativePath )
    {
        Dependency dependency = new Dependency();
        dependency.setRelativePath( relativePath );
        return dependency;
    }

    private Directory createDirectory( String relativePath )
    {
        Directory directory = new Directory();
        directory.setRelativePath( relativePath );
        return directory;
    }
}
