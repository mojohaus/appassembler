package org.codehaus.mojo.appassembler.daemon.script;

import junit.framework.TestCase;

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
        test( Platform.getInstance( "unix" ), new String[] {
            "/foo/bar",
            "bar/foo",
            "/foo/bar:\"$BASEDIR\"/bar/foo",
            "group/artifact/version/artifact-version.jar",
            "/foo/bar:\"$BASEDIR\"/bar/foo:\"$REPO\"/group/artifact/version/artifact-version.jar",
        });
    }

    public void testCreateClassPathWindows()
        throws Exception
    {
        test( Platform.getInstance( "windows" ), new String[] {
            "/foo/bar",
            "bar/foo",
            "/foo/bar;\"%BASEDIR%\"\\bar/foo",
            "group/artifact/version/artifact-version.jar",
            "/foo/bar;\"%BASEDIR%\"\\bar/foo;\"%REPO%\"\\group/artifact/version/artifact-version.jar",
        });
    }

    public void testGetAppArgumentsUnix()
        throws Exception
    {
        for ( Iterator it = Platform.getPlatformSet( Collections.singletonList( "all" ) ).iterator(); it.hasNext(); )
        {
            Platform platform = (Platform) it.next();

            testGetAppArguments( platform );
        }
    }

    private void testGetAppArguments( Platform util )
        throws Exception
    {
        Daemon daemon = new Daemon();
        assertEquals( null, util.getAppArguments( daemon ) );

        List commandLineArguments = new ArrayList();
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

        assertEquals( "", util.getClassPath( daemon ) );

        List classpath = daemon.getClasspath();
        classpath.add( createDirectory( asserts[ 0 ] ) );
        classpath.add( createDirectory( asserts[ 1 ] ) );
        assertEquals( asserts[ 2 ], util.getClassPath( daemon ) );

        classpath.add( createDependency( asserts[ 3 ]) );
        assertEquals( asserts[ 4 ], util.getClassPath( daemon ) );
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
