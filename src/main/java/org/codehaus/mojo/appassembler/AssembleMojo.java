package org.codehaus.mojo.appassembler;

/**
 * The MIT License
 *
 * Copyright 2005-2006 The Codehaus.
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
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

/**
 *
 *
 * @goal assemble
 * @requiresDependencyResolution runtime
 * @phase package
 * @description
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 */
public class AssembleMojo
    extends AbstractMojo
{
    /**
     *
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String buildDirectory;

    /**
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}.${project.packaging}"
      */
    private String artifactFinalName;

    /**
     *
     * @parameter expression="${project.artifacts}"
     * @required
     */
    private Set artifacts;

    /**
     *
     * @parameter expression="${project.build.directory}/${project.artifactId}-${project.version}"
     */
    private String assembleDirectory;

    /**
     *
     * @parameter expression="${project.artifact}"
     */
    private Artifact projectArtifact;

    /**
     *
     * @component org.apache.maven.artifact.repository.ArtifactRepositoryFactory
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     *
     * @component org.apache.maven.artifact.installer.ArtifactInstaller
     */
    private ArtifactInstaller artifactInstaller;

    /**
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    private ArtifactRepository artifactRepository;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        artifactRepository = artifactRepositoryFactory.createDeploymentArtifactRepository(
            "appassembler", "file://" + assembleDirectory + "/repo", new DefaultRepositoryLayout(), false );

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            File artifactFile = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );

            installArtifact( artifact, artifactFile );
        }

        File projectArtifactFile = new File( artifactFinalName );

        installArtifact( projectArtifact, projectArtifactFile );
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
