package org.codehaus.mojo.appassembler.daemon.booter;

import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.script.ScriptGenerator;
import org.codehaus.mojo.appassembler.daemon.script.Platform;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractBooterDaemonGenerator
    implements DaemonGenerator
{
    /**
     * @plexus.requirement role-hint="generic"
     */
    private DaemonGenerator genericDaemonGenerator;

    /**
     * @plexus.requirement
     */
    private ScriptGenerator scriptGenerator;

    private boolean isWindows;

    protected AbstractBooterDaemonGenerator( boolean windows )
    {
        isWindows = windows;
    }

    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    public void generate( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        Daemon daemon = request.getDaemon();

        String platformName = isWindows ? Platform.WINDOWS_NAME : Platform.UNIX_NAME;

        File outputDirectory = request.getOutputDirectory();

        // -----------------------------------------------------------------------
        // Generate the generic XML file
        // -----------------------------------------------------------------------

        request.setOutputDirectory( new File( outputDirectory, "etc" ) );

        genericDaemonGenerator.generate( request  );

        // -----------------------------------------------------------------------
        // Generate the shell script
        // -----------------------------------------------------------------------

        daemon.setMainClass( "org.codehaus.mojo.appassembler.booter.AppassemblerBooter" );

        MavenProject project = request.getMavenProject();

        List classpath = new ArrayList( 2 );

        // TODO: Transitively resulve the dependencies of the booter.
        addDirectory( classpath, "etc" );
        addArtifact( classpath, project, "org.codehaus.mojo:appassembler-booter" );
        addArtifact( classpath, project, "org.codehaus.mojo:appassembler-model" );

        daemon.setClasspath( classpath );

        scriptGenerator.createBinScript( platformName,
                                         daemon,
                                         outputDirectory );
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private void addArtifact( List classpath, MavenProject project, String id )
        throws DaemonGeneratorException
    {
        Artifact artifact = (Artifact) project.getArtifactMap().get( id );

        if ( artifact == null )
        {
            throw new DaemonGeneratorException( "The project has to have a dependency on '" + id + "'." );
        }

        String versionNumber = artifact.getVersion();
        
        String groupIdPart = id.substring( 0,id.indexOf( ":") ).replace( '.', '/');
        String artifactIdPart = id.substring(id.indexOf( ":") + 1, id.length() );
        
        String relativePath = groupIdPart + "/" + artifactIdPart + "/" + versionNumber + "/" + artifactIdPart + "-" +
            versionNumber + ".jar";

        Dependency dependency = new Dependency();
        dependency.setRelativePath( relativePath );
        classpath.add( dependency );
    }

    private void addDirectory( List classpath, String relativePath )
    {
        Directory directory = new Directory();
        directory.setRelativePath( relativePath );
        classpath.add( directory );
    }
}
