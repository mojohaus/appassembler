package org.codehaus.mojo.appassembler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.repository.layout.LegacyRepositoryLayout;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.codehaus.mojo.appassembler.repository.FlatRepositoryLayout;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Creates an appassembler repository.
 * 
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 * @goal create-repository
 * @requiresDependencyResolution runtime
 * @phase package
 */
public class CreateRepositoryMojo extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // parameters
    // -----------------------------------------------------------------------

    /**
     * The directory that will be used to assemble the artifacts in and place the bin scripts.
     * 
     * @required
     * @parameter expression="${project.build.directory}/appassembler"
     */
    private File assembleDirectory;

    /**
     * The directory that will be used for the dependencies, relative to assembleDirectory.
     * 
     * @required
     * @parameter expression="repo"
     */
    private String repoPath;

    /**
     * The layout of the generated Maven repository. Supported types - "default" (Maven2) | "legacy" (Maven1) | "flat"
     * (flat lib/ style)
     * 
     * @parameter default="default'
     */
    private String repositoryLayout;

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
     * @parameter expression="${plugin.artifacts}"
     */
    private List pluginArtifacts;

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

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // ----------------------------------------------------------------------
        // Create new repository for dependencies
        // ----------------------------------------------------------------------

        if ( repositoryLayout == null || repositoryLayout.equals( "default" ) )
        {
            artifactRepositoryLayout = new DefaultRepositoryLayout();
        }
        else if ( repositoryLayout.equals( "legacy" ) )
        {
            artifactRepositoryLayout = new LegacyRepositoryLayout();
        }
        else if ( repositoryLayout.equals( "flat" ) )
        {
            artifactRepositoryLayout = new FlatRepositoryLayout();
        }
        else
        {
            throw new MojoFailureException( "Unknown repository layout '" + repositoryLayout + "'." );
        }

        // -----------------------------------------------------------------------
        // Initialize
        // -----------------------------------------------------------------------

        artifactRepository =
            artifactRepositoryFactory.createDeploymentArtifactRepository( "appassembler", "file://"
                            + assembleDirectory.getAbsolutePath() + "/" + repoPath, artifactRepositoryLayout, false );

        // -----------------------------------------------------------------------
        // Install the project's artifact in the new repository
        // -----------------------------------------------------------------------

        installArtifact( projectArtifact );

        // ----------------------------------------------------------------------
        // Install dependencies in the new repository
        // ----------------------------------------------------------------------

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            installArtifact( artifact );
        }
        
        for ( Iterator it = pluginArtifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            installArtifact( artifact );
        }

        // ----------------------------------------------------------------------
        // Install appassembler booter in the new repos
        // ----------------------------------------------------------------------
        Artifact booter = resolveBooterArtifact();
        //installArtifact( booter );
    }

    private void installArtifact( Artifact artifact ) throws MojoExecutionException
    {
        try
        {
            // Necessary for the artifact's baseVersion to be set correctly
            // See: http://mail-archives.apache.org/mod_mbox/maven-dev/200511.mbox/%3c437288F4.4080003@apache.org%3e
            artifact.isSnapshot();

            artifactInstaller.install( artifact.getFile(), artifact, artifactRepository );
        }
        catch ( ArtifactInstallationException e )
        {
            throw new MojoExecutionException( "Failed to copy artifact.", e );
        }
    }

    protected Artifact resolveBooterArtifact() throws MojoExecutionException
    {
        Artifact booter;
        booter =
            new DefaultArtifact( "org.codehaus.mojo", "appassembler-booter",
                                 VersionRange.createFromVersion( "1.0-SNAPSHOT" ), "compile", "jar", "",
                                 new DefaultArtifactHandler( "" ) );

        return booter;
    }
}
