package org.codehaus.mojo.appassembler.daemon.generic;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.Util;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.merge.DaemonMerger;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.io.DaemonModelUtil;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role-hint="generic"
 */
public class GenericDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{
    /**
     * @plexus.requirement
     */
    private DaemonMerger daemonMerger;

    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    public void generate( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        // -----------------------------------------------------------------------
        // Create the daemon from the Maven project
        // -----------------------------------------------------------------------

        Daemon createdDaemon = createDaemon( request.getMavenProject(), request.getRepositoryPath(),
                request.getRepositoryLayout() );

        // -----------------------------------------------------------------------
        // Merge the given stub daemon and the generated
        // -----------------------------------------------------------------------

        Daemon mergedDaemon = daemonMerger.mergeDaemons( request.getDaemon(), createdDaemon );

        // -----------------------------------------------------------------------
        // Write out the project
        // -----------------------------------------------------------------------

        FileWriter writer = null;

        try
        {
            FileUtils.forceMkdir( request.getOutputDirectory() );

            File outputFile = new File( request.getOutputDirectory(), mergedDaemon.getId() + ".xml" );

            writer = new FileWriter( outputFile );

            DaemonModelUtil.storeModel( mergedDaemon, outputFile );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error while writing output file: " + request.getOutputDirectory(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private Daemon createDaemon( MavenProject project, String repositoryPath, ArtifactRepositoryLayout layout )
    {
        Daemon complete = new Daemon();

        // -----------------------------------------------------------------------
        // Add the project itself as a dependency.
        // -----------------------------------------------------------------------
        complete.setClasspath( new ArrayList() );
        Dependency projectDependency = new Dependency();
        Artifact projectArtifact = project.getArtifact();
        projectDependency.setGroupId( projectArtifact.getGroupId() );
        projectDependency.setArtifactId( projectArtifact.getArtifactId() );
        projectDependency.setVersion( projectArtifact.getVersion() );
        projectDependency.setClassifier( projectArtifact.getClassifier() );
        projectDependency.setRelativePath( Util.getRelativePath( projectArtifact, layout ) );
        complete.getClasspath().add( projectDependency );

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

            dependency.setRelativePath( Util.getRelativePath( artifact, layout ));

            complete.getClasspath().add( dependency );
        }

        return complete;
    }
}
