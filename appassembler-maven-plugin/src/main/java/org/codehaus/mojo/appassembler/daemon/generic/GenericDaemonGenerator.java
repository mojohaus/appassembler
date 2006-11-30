package org.codehaus.mojo.appassembler.daemon.generic;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.mojo.appassembler.model.io.xpp3.AppassemblerModelXpp3Writer;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.Util;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role-hint="generic"
 */
public class GenericDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{
    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    public void generate( Daemon stubDaemon, MavenProject project, ArtifactRepository localRepository,
                          File outputDirectory )
        throws DaemonGeneratorException
    {
        Daemon mergedDaemon = mergeDaemon( stubDaemon, project, new JvmSettings() );

        AppassemblerModelXpp3Writer writer = new AppassemblerModelXpp3Writer();

        try
        {
            FileUtils.forceMkdir( outputDirectory );

            File outputFile = new File( outputDirectory, mergedDaemon.getId() + ".xml" );

            writer.write( new FileWriter( outputFile ), mergedDaemon );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error while writing output file: " + outputDirectory, e );
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private Daemon mergeDaemon( Daemon stub, MavenProject project, JvmSettings defaultJvmSettings )
    {
        Daemon complete = new Daemon();

        complete.setId( stub.getId() );

        complete.setMainClass( stub.getMainClass() );

        complete.setJvmSettings( mergeJvmSettings( stub.getJvmSettings(), defaultJvmSettings ) );

        complete.setCommandLineArguments( stub.getCommandLineArguments() );

        // -----------------------------------------------------------------------
        // Add the project itself as a dependency.
        // -----------------------------------------------------------------------

        complete.setDependencies( new ArrayList() );
        Dependency projectDependency = new Dependency();
        Artifact projectArtifact = project.getArtifact();
        projectDependency.setGroupId( projectArtifact.getGroupId() );
        projectDependency.setArtifactId( projectArtifact.getArtifactId() );
        projectDependency.setVersion( projectArtifact.getVersion() );
        projectDependency.setClassifier( projectArtifact.getClassifier() );
        projectDependency.setRelativePath( Util.getRelativePath( projectArtifact ));
        complete.getDependencies().add( projectDependency );

        // -----------------------------------------------------------------------
        // Add all the dependencies from the project.
        // -----------------------------------------------------------------------

        for ( Iterator it = project.getRuntimeArtifacts().iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            Dependency dependency = new Dependency();
            dependency.setGroupId( artifact.getGroupId() );
            dependency.setArtifactId( artifact.getArtifactId() );
            dependency.setVersion( artifact.getVersion() );
            dependency.setClassifier( artifact.getClassifier() );
            dependency.setRelativePath( Util.getRelativePath( artifact ));

            complete.getDependencies().add( dependency );
        }

        return complete;
    }

    private JvmSettings mergeJvmSettings( JvmSettings stubJvmSettings, JvmSettings defaultJvmSettings )
    {
        if ( stubJvmSettings == null )
        {
            return defaultJvmSettings;
        }

        JvmSettings complete = new JvmSettings();

        complete.setInitialMemorySize( mergeString( stubJvmSettings.getInitialMemorySize(), defaultJvmSettings.getInitialMemorySize() ));
        complete.setMaxMemorySize( mergeString( stubJvmSettings.getMaxMemorySize(), defaultJvmSettings.getMaxMemorySize() ));
        complete.setMaxStackSize( mergeString( stubJvmSettings.getMaxStackSize(), defaultJvmSettings.getMaxStackSize() ));

        return complete;
    }

    private String mergeString( String stub, String defaultString )
    {
        if ( StringUtils.isEmpty( stub ) )
        {
            return defaultString;
        }

        return stub;
    }
}
