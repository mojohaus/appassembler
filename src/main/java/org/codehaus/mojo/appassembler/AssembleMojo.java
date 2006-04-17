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
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;

import java.io.*;
import java.util.*;

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

    /**
     * @parameter
     */
    private Set mainClasses;

    /**
     * Prefix generated bin files with this
     *
     * @parameter
     */
    private String binPrefix;

    /**
     *
     */
    private String classPath = "";

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // ----------------------------------------------------------------------
        // Create new repository for dependencies
        // ----------------------------------------------------------------------

        artifactRepository = artifactRepositoryFactory.createDeploymentArtifactRepository(
            "appassembler", "file://" + assembleDirectory + "/repo", new DefaultRepositoryLayout(), false );

        // ----------------------------------------------------------------------
        // Install dependencies in the new repository
        // ----------------------------------------------------------------------

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            File artifactFile = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );

            installArtifact( artifact, artifactFile );
        }

        File projectArtifactFile = new File( artifactFinalName );

        installArtifact( projectArtifact, projectArtifactFile );

        // ----------------------------------------------------------------------
        // Setup
        // ----------------------------------------------------------------------

        setUp();

        // ----------------------------------------------------------------------
        // Generate bin files for main classes
        // ----------------------------------------------------------------------

        for ( Iterator it = mainClasses.iterator(); it.hasNext(); )
        {
            String mainClass = (String) it.next();

            try
            {
                InputStream in = this.getClass().getResourceAsStream( "/binTemplate" );

                InputStreamReader reader = new InputStreamReader( in );

                Map context = new HashMap();
                context.put( "MAINCLASS", mainClass );
                context.put( "CLASSPATH", classPath );

                InterpolationFilterReader interpolationFilterReader = new InterpolationFilterReader( reader, context, "@", "@" );

                // Get class name and use it as the filename
                String binFileName = "";
                StringTokenizer tokenizer = new StringTokenizer( mainClass, "." );
                while ( tokenizer.hasMoreElements() )
                {
                    binFileName = tokenizer.nextToken();
                }

                if ( binPrefix == null )
                {
                    binFileName = binFileName.toLowerCase();
                }
                else
                {
                    binFileName = binPrefix.trim() + binFileName.toLowerCase();
                }

                File binFile = new File( assembleDirectory + "/bin", binFileName );
                FileWriter out = new FileWriter( binFile );

                IOUtil.copy( interpolationFilterReader, out );

                interpolationFilterReader.close();
                out.close();
            }
            catch ( FileNotFoundException e )
            {
                  throw new MojoExecutionException( "Failed to get template for bin file.", e );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to write bin file.", e );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Install artifacts into the assemble repository
    // ----------------------------------------------------------------------

    private void installArtifact( Artifact artifact, File artifactFile )
        throws MojoExecutionException
    {
        if ( artifactFile.exists() )
        {
            try
            {
                artifactInstaller.install( artifactFile, artifact, artifactRepository );

                addToClassPath( localRepository.pathOf( artifact ));
            }
            catch ( ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "Failed to copy artifact.", e );
            }
        }

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void addToClassPath( String classPathEntry )
    {
        classPath += "$REPO/" + classPathEntry + ":";
    }

    // ----------------------------------------------------------------------
    // Set up the assemble environment
    // ----------------------------------------------------------------------

    private void setUp()
        throws MojoFailureException
    {
        // create (if necessary) directory for bin files
        File binDir = new File( assembleDirectory, "bin" );

        if ( !binDir.exists() )
        {
            boolean success = new File( assembleDirectory, "bin" ).mkdir();

            if ( !success )
            {
                throw new MojoFailureException( "Failed to create directory for bin files." );
            }
        }
    }
}
