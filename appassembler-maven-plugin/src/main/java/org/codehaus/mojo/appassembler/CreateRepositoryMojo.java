package org.codehaus.mojo.appassembler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

/**
 * Creates an appassembler repository.
 *
 * @goal create-repository
 * @requiresDependencyResolution runtime
 * @phase package
 *
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 */
public class CreateRepositoryMojo
    extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // parameters
    // -----------------------------------------------------------------------

    /**
     * The directory that will be used to assemble the artifacts in
     * and place the bin scripts.
     *
     * @required
     * @parameter expression="${project.build.directory}/appassembler"
     */
    private File assembleDirectory;

    // -----------------------------------------------------------------------
    // Read-only parameters
    // -----------------------------------------------------------------------

    /**
     * @readonly
     * @parameter expression="${project.artifacts}"
     */
    private Set artifacts;

    /**
     * @readonly
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;
    
    /**
     * @readonly
     * @parameter expression="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     */
    private String artifactFinalName;

    /**
     * @readonly
     * @parameter expression="${project.artifact}"
     */
    private Artifact projectArtifact;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /**
     * @component org.apache.maven.artifact.repository.ArtifactRepositoryFactory
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @component org.apache.maven.artifact.installer.ArtifactInstaller
     */
    private ArtifactInstaller artifactInstaller;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    /**
     * The repo where the jar files will be installed
     */
    private ArtifactRepository artifactRepository;

    /**
     * The layout of the repository.
     */
    private ArtifactRepositoryLayout artifactRepositoryLayout;

    // -----------------------------------------------------------------------
    // AbstractMojo Implementation
    // -----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // -----------------------------------------------------------------------
        // Initialize
        // -----------------------------------------------------------------------

        artifactRepositoryLayout = new DefaultRepositoryLayout();

        artifactRepository = artifactRepositoryFactory.createDeploymentArtifactRepository( "appassembler",
            "file://" + assembleDirectory.getAbsolutePath() + "/repo", artifactRepositoryLayout, false );

        // -----------------------------------------------------------------------
        // Install the project's artifact in the new repository
        // -----------------------------------------------------------------------

        File projectArtifactFile = new File( artifactFinalName );

        installArtifact( projectArtifact, projectArtifactFile );

        // ----------------------------------------------------------------------
        // Install dependencies in the new repository
        // ----------------------------------------------------------------------

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            File artifactFile = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );

            installArtifact( artifact, artifactFile );
        }
    }

    private void installArtifact( Artifact artifact, File artifactFile )
        throws MojoExecutionException
    {
        if ( artifactFile.exists() )
        {
            try
            {
                artifactInstaller.install( artifactFile, artifact, artifactRepository );
            }
            catch ( ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "Failed to copy artifact.", e );
            }
        }
    }
}
