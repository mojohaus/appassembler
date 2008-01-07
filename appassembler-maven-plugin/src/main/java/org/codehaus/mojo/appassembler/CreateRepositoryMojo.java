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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * The directory that will be used for the dependencies, relative to assembleDirectory.
     *
     * @required
     * @parameter expression="repo"
     * @todo customisation doesn't work due to the shell scripts not honouring it
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
     * @component
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @component
     */
    private ArtifactInstaller artifactInstaller;

    /**
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map availableRepositoryLayouts;

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

        ArtifactRepository artifactRepository = artifactRepositoryFactory.createDeploymentArtifactRepository(
            "appassembler", path, artifactRepositoryLayout, false );

        // -----------------------------------------------------------------------
        // Install the project's artifact in the new repository
        // -----------------------------------------------------------------------

        installArtifact( projectArtifact, artifactRepository );

        // ----------------------------------------------------------------------
        // Install dependencies in the new repository
        // ----------------------------------------------------------------------

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            installArtifact( artifact, artifactRepository );
        }

        for ( Iterator it = pluginArtifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            installArtifact( artifact, artifactRepository );
        }

        // ----------------------------------------------------------------------
        // Install appassembler booter in the new repos
        // ----------------------------------------------------------------------
        Artifact booter = resolveBooterArtifact();
        //installArtifact( booter );
    }

    private void installArtifact( Artifact artifact, ArtifactRepository artifactRepository )
        throws MojoExecutionException
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

    protected Artifact resolveBooterArtifact()
        throws MojoExecutionException
    {
        Artifact booter;
        booter = new DefaultArtifact( "org.codehaus.mojo", "appassembler-booter",
                                      VersionRange.createFromVersion( "1.0-SNAPSHOT" ), "compile", "jar", "",
                                      new DefaultArtifactHandler( "" ) );

        return booter;
    }

    public void setAvailableRepositoryLayouts( Map availableRepositoryLayouts )
    {
        this.availableRepositoryLayouts = availableRepositoryLayouts;
    }
}
