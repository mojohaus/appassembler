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
package org.codehaus.mojo.appassembler;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Creates an appassembler repository. Note that this is deliberately a bit more specific than the assembly plugin
 * version - if that could generate a flat layout and exclude JARs, it may be a suitable replacement.
 * 
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 * @goal create-repository
 * @requiresDependencyResolution runtime
 * @phase package
 * @threadSafe
 */
public class CreateRepositoryMojo
    extends AbstractAppAssemblerMojo
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
     * Whether to install the booter artifacts into the repository. This may be needed if you are using the Shell script
     * generators.
     * 
     * @parameter default-value="false"
     */
    private boolean installBooterArtifacts;

    /**
     * The directory that will be used for the dependencies, relative to <code>assembleDirectory</code>.
     * 
     * @required
     * @parameter default-value="repo"
     * @deprecated Use <code>repositoryName</code> instead.
     * @todo customisation doesn't work due to the shell scripts not honouring it
     */
    private String repoPath;

    /**
     * Path (relative to <code>assembleDirectory</code>) of the desired output repository.
     * 
     * @parameter default-value="repo"
     * @since 1.4
     * @todo Customization doesn't work due to the shell scripts not honouring it
     */
    private String repositoryName;

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

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /** @component */
    private ArtifactFactory artifactFactory;

    /** @component */
    private ArtifactResolver artifactResolver;

    /**
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map availableRepositoryLayouts;

    /** @component */
    private ArtifactMetadataSource metadataSource;

    // -----------------------------------------------------------------------
    // AbstractMojo Implementation
    // -----------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
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

        StringBuffer path = new StringBuffer( "file://" + assembleDirectory.getAbsolutePath() + "/" );

        // If repositoryName is configured to anything but the default value - use it
        if ( !"repo".equals( repositoryName ) )
        {
            path.append( repositoryName );
        }
        else
        {
            // Fall back to deprecated parameter for backwards compatibility
            path.append( repoPath );
        }

        ArtifactRepository artifactRepository =
            artifactRepositoryFactory.createDeploymentArtifactRepository( "appassembler", path.toString(),
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

            installArtifact( artifact, artifactRepository, this.useTimestampInSnapshotFileName );
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
                installArtifact( a, artifactRepository, this.useTimestampInSnapshotFileName );
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

    /**
     * Set the available repository layouts.
     * 
     * @param availableRepositoryLayouts
     */
    public void setAvailableRepositoryLayouts( Map availableRepositoryLayouts )
    {
        this.availableRepositoryLayouts = availableRepositoryLayouts;
    }

}
