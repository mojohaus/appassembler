package org.codehaus.mojo.appassembler;

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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Creates an appassembler repository. Note that this is deliberately a bit more specific than the assembly plugin
 * version - if it can generate a flat layout and exclude JARs, it may be a suitable replacement.
 * 
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 * @goal create-repository
 * @requiresDependencyResolution runtime
 * @phase package
 */
public class CreateRepositoryMojo
    extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * The directory that will be used to assemble the artifacts in and place the bin scripts.
     * 
     * @required
     * @parameter expression="${project.build.directory}/appassembler"
     */
    private File assembleDirectory;

    /**
     * The directory that will be used for the dependencies, relative to <code>assembleDirectory</code>.
     * 
     * @required
     * @parameter default-value="repo"
     * @todo customisation doesn't work due to the shell scripts not honouring it
     */
    private String repoPath;

    /**
     * The layout of the generated Maven repository. Supported types - "default" (Maven2) | "legacy" (Maven1) | "flat"
     * (flat <code>lib/</code> style).
     * 
     * @parameter default-value="default"
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
     * @parameter expression="${plugin.version}"
     */
    private String pluginVersion;

    /**
     * @readonly
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * @readonly
     * @parameter expression="${project.artifact}"
     */
    private Artifact projectArtifact;

    /**
     * Whether to install the booter artifacts into the repository. This may be needed if you are using the Shell script
     * generators.
     * 
     * @parameter default-value="false"
     */
    private boolean installBooterArtifacts;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /** @component */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map availableRepositoryLayouts;

    /** @component */
    private ArtifactResolver artifactResolver;

    /** @component */
    private ArtifactMetadataSource metadataSource;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // ----------------------------------------------------------------------
        // Create new repository for dependencies
        // ----------------------------------------------------------------------

        ArtifactRepositoryLayout artifactRepositoryLayout =
            (ArtifactRepositoryLayout) availableRepositoryLayouts.get( repositoryLayout );
        if ( artifactRepositoryLayout == null )
        {
            throw new MojoFailureException( "Unknown repository layout '" + repositoryLayout + "'." );
        }

        // -----------------------------------------------------------------------
        // Initialize
        // -----------------------------------------------------------------------

        String path = "file://" + assembleDirectory.getAbsolutePath() + "/" + repoPath;

        ArtifactRepository artifactRepository =
            artifactRepositoryFactory.createDeploymentArtifactRepository( "appassembler", path,
                                                                          artifactRepositoryLayout, true );

        // -----------------------------------------------------------------------
        // Install the project's artifact in the new repository
        // -----------------------------------------------------------------------

        installArtifact( projectArtifact, artifactRepository );

        // ----------------------------------------------------------------------
        // Install dependencies in the new repository
        // ----------------------------------------------------------------------

        // TODO: merge with the artifacts below so no duplicate versions included
        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            installArtifact( artifact, artifactRepository );
        }

        if ( installBooterArtifacts )
        {
            // ----------------------------------------------------------------------
            // Install appassembler booter in the new repos
            // ----------------------------------------------------------------------
            installBooterArtifacts( artifactRepository );
        }
    }

    private void installBooterArtifacts( ArtifactRepository artifactRepository )
        throws MojoExecutionException
    {
        Artifact artifact =
            artifactFactory.createDependencyArtifact( "org.codehaus.mojo.appassembler", "appassembler-booter",
                                                      VersionRange.createFromVersion( pluginVersion ), "jar", null,
                                                      Artifact.SCOPE_RUNTIME );
        try
        {
            Artifact p =
                artifactFactory.createBuildArtifact( "org.codehaus.mojo.appassembler", "appassembler-maven-plugin",
                                                     pluginVersion, "jar" );

            ArtifactFilter filter = new ExcludesArtifactFilter( Collections.singletonList( "junit:junit" ) );
            ArtifactResolutionResult result =
                artifactResolver.resolveTransitively( Collections.singleton( artifact ), p, localRepository,
                                                      Collections.EMPTY_LIST, metadataSource, filter );
            for ( Iterator i = result.getArtifacts().iterator(); i.hasNext(); )
            {
                Artifact a = (Artifact) i.next();
                installArtifact( a, artifactRepository );
            }
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Failed to copy artifact.", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "Failed to copy artifact.", e );
        }
    }

    private void installArtifact( Artifact artifact, ArtifactRepository artifactRepository )
        throws MojoExecutionException
    {
        if ( artifact.getFile() != null )
        {
            try
            {
                // Necessary for the artifact's baseVersion to be set correctly
                // See: http://mail-archives.apache.org/mod_mbox/maven-dev/200511.mbox/%3c437288F4.4080003@apache.org%3e
                artifact.isSnapshot();

                install( artifact.getFile(), artifact, artifactRepository );
            }
            catch ( ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "Failed to copy artifact.", e );
            }
        }
    }

    public void setAvailableRepositoryLayouts( Map availableRepositoryLayouts )
    {
        this.availableRepositoryLayouts = availableRepositoryLayouts;
    }

    public void install( File source, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException
    {
        try
        {
            String localPath = localRepository.pathOf( artifact );

            File destination = new File( localRepository.getBasedir(), localPath );
            if ( !destination.getParentFile().exists() )
            {
                destination.getParentFile().mkdirs();
            }

            getLog().info( "Installing artifact " + source.getPath() + " to " + destination );

            FileUtils.copyFile( source, destination );

        }
        catch ( IOException e )
        {
            throw new ArtifactInstallationException( "Error installing artifact: " + e.getMessage(), e );
        }
    }
}
