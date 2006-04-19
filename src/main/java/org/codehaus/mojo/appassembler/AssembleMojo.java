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
 * @goal assemble
 * @requiresDependencyResolution runtime
 * @phase package
 * @description
 *
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id:$
 */
public class AssembleMojo
    extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------------

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String buildDirectory;

    /**
     * @parameter expression="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     */
    private String artifactFinalName;

    /**
     * @parameter expression="${project.artifacts}"
     * @required
     */
    private Set artifacts;

    /**
     * @parameter expression="${project.build.directory}/${project.artifactId}-${project.version}"
     */
    private File assembleDirectory;

    /**
     * @parameter expression="${project.artifact}"
     */
    private Artifact projectArtifact;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter
     */
    private Set programs;

    /**
     * Prefix generated bin files with this.
     *
     * @parameter
     */
    private String binPrefix;

    /**
     * @parameter default-value="true"
     */
    private boolean includeConfigurationDirectoryInClasspath;

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

    private String classPath = "";

    private ArtifactRepository artifactRepository;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // ----------------------------------------------------------------------
        // Create new repository for dependencies
        // ----------------------------------------------------------------------

        artifactRepository = artifactRepositoryFactory.createDeploymentArtifactRepository(
            "appassembler", "file://" + assembleDirectory.getAbsolutePath() + "/repo", new DefaultRepositoryLayout(), false );

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

        setUpWorkingArea();

        if ( includeConfigurationDirectoryInClasspath )
        {
            // TODO: This is UNIX-specific
            // TODO: make "etc" configurable
            classPath = "\"$BASEDIR\"/etc:" + classPath;
        }

        // ----------------------------------------------------------------------
        // Generate bin files for main classes
        // ----------------------------------------------------------------------

        for ( Iterator it = programs.iterator(); it.hasNext(); )
        {
            Program program = (Program) it.next();

            try
            {
                InputStream in = this.getClass().getResourceAsStream( "/binTemplate" );

                InputStreamReader reader = new InputStreamReader( in );

                Map context = new HashMap();
                context.put( "MAINCLASS", program.getMainClass() );
                context.put( "CLASSPATH", classPath );

                InterpolationFilterReader interpolationFilterReader = new InterpolationFilterReader( reader, context, "@", "@" );

                // Set the name of the bin file
                String binFileName = "";

                if ( program.getName() == null || program.getName().trim().equals( "" ) )
                {
                    // Get class name and use it as the filename
                    StringTokenizer tokenizer = new StringTokenizer( program.getMainClass(), "." );
                    while ( tokenizer.hasMoreElements() )
                    {
                        binFileName = tokenizer.nextToken();
                    }

                    binFileName.toLowerCase();
                }
                else
                {
                    binFileName = program.getName();
                }

                // Set bin prefix
                if ( binPrefix != null )
                {
                    binFileName = binPrefix.trim() + binFileName;
                }

                File binFile = new File( assembleDirectory.getAbsolutePath() + "/bin", binFileName );
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

    private void setUpWorkingArea()
        throws MojoFailureException
    {
        // create (if necessary) directory for bin files
        File binDir = new File( assembleDirectory.getAbsolutePath(), "bin" );

        if ( !binDir.exists() )
        {
            boolean success = new File( assembleDirectory.getAbsolutePath() , "bin" ).mkdir();

            if ( !success )
            {
                throw new MojoFailureException( "Failed to create directory for bin files." );
            }
        }
    }
}
