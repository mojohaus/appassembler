package org.codehaus.mojo.appassembler;

/*
 * The MIT License
 *
 * Copyright (c) 2006-2012, The Codehaus
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
import java.util.Collections;
import java.util.Iterator;
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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Creates an appassembler repository. Note that this is deliberately a bit more specific than the assembly plugin
 * version - if that could generate a flat layout and exclude JARs, it may be a suitable replacement.
 *
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 */
@Mojo( name = "create-repository", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true )
public class CreateRepositoryMojo
    extends AbstractAppAssemblerMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * The directory that will be used to assemble the artifacts in and place the bin scripts.
     */
    @Parameter( defaultValue = "${project.build.directory}/appassembler", required = true )
    private File assembleDirectory;

    /**
     * Whether to install the booter artifacts into the repository. This may be needed if you are using the Shell script
     * generators.
     */
    @Parameter( defaultValue = "false" )
    private boolean installBooterArtifacts;

    /**
     * Path (relative to <code>assembleDirectory</code>) of the desired output repository.
     *
     * @since 1.3.1
     * @todo Customization doesn't work due to the shell scripts not honouring it
     */
    @Parameter( defaultValue = "repo" )
    private String repositoryName;

    // -----------------------------------------------------------------------
    // Read-only parameters
    // -----------------------------------------------------------------------

    @Parameter( defaultValue = "${project.artifacts}", readonly = true )
    private Set artifacts;

    @Parameter( defaultValue = "${plugin.version}", readonly = true )
    private String pluginVersion;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    @Component
    private ArtifactFactory artifactFactory;

    @Component
    private ArtifactResolver artifactResolver;

    @Component
    private ArtifactMetadataSource metadataSource;

    // -----------------------------------------------------------------------
    // AbstractMojo Implementation
    // -----------------------------------------------------------------------

    /**
     * calling from Maven.
     *
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // ----------------------------------------------------------------------
        // Create new repository for dependencies
        // ----------------------------------------------------------------------

        ArtifactRepositoryLayout artifactRepositoryLayout = getArtifactRepositoryLayout();

        // -----------------------------------------------------------------------
        // Initialize
        // -----------------------------------------------------------------------

        StringBuffer path = new StringBuffer( "file://" + assembleDirectory.getAbsolutePath() + "/" );

        path.append( repositoryName );

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

}
