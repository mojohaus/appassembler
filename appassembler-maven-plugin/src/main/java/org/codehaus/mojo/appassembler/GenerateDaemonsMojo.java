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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal generate-daemons
 * @requiresDependencyResolution runtime
 * @phase generate-resources
 */
public class GenerateDaemonsMojo
    extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * @parameter
     * @required
     */
    private Set daemons;

    /**
     * @parameter
     */
    private JvmSettings defaultJvmSettings;

    /**
     * The directory that will be used for the dependencies, relative to assembleDirectory.
     *
     * @required
     * @parameter expression="${project.build.directory}/appassembler/repo"
     */
    private String repoPath;

    /**
     * @parameter expression="${basedir}"
     * @required
     */
    private File basedir;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File target;

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

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
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /**
     * @component org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService
     */
    private DaemonGeneratorService daemonGeneratorService;

    // -----------------------------------------------------------------------
    // AbstractMojo Implementation
    // -----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            for ( Iterator it = daemons.iterator(); it.hasNext(); )
            {
                Daemon daemon = (Daemon) it.next();

                // -----------------------------------------------------------------------
                // Load the optional template daemon descriptor
                // -----------------------------------------------------------------------

                File descriptor = null;

                if ( !StringUtils.isEmpty( daemon.getDescriptor() ) )
                {
                    descriptor = new File( basedir, daemon.getDescriptor() );
                }

                // -----------------------------------------------------------------------
                //
                // -----------------------------------------------------------------------

                org.codehaus.mojo.appassembler.model.JvmSettings modelJvmSettings = null;

                if ( defaultJvmSettings != null )
                {
                    modelJvmSettings = convertJvmSettings( defaultJvmSettings );
                }

                // -----------------------------------------------------------------------
                // Create a daemon object from the POM configuration
                // -----------------------------------------------------------------------

                org.codehaus.mojo.appassembler.model.Daemon modelDaemon;

                modelDaemon = new org.codehaus.mojo.appassembler.model.Daemon();

                modelDaemon.setId( daemon.getId() );
                modelDaemon.setMainClass( daemon.getMainClass() );
                modelDaemon.setCommandLineArguments( daemon.getCommandLineArguments() );

                if ( daemon.getJvmSettings() != null )
                {
                    modelDaemon.setJvmSettings( convertJvmSettings( daemon.getJvmSettings() ) );
                }
                else
                {
                    modelDaemon.setJvmSettings( modelJvmSettings );
                }

                // -----------------------------------------------------------------------
                //
                // -----------------------------------------------------------------------

                for ( Iterator it2 = daemon.getPlatforms().iterator(); it2.hasNext(); )
                {
                    String platform = (String) it2.next();

                    File output = new File( new File( target, "generated-resources" ), platform );

                    DaemonGenerationRequest request = new DaemonGenerationRequest();

                    request.setPlatform( platform );
                    request.setStubDescriptor( descriptor );
                    request.setStubDaemon( modelDaemon );
                    request.setOutputDirectory( output );
                    request.setMavenProject( project );
                    request.setLocalRepository( localRepository );
                    request.setRepositoryLayout( Util.getRepositoryLayout( repositoryLayout ) );
                    request.setRepositoryPath( repoPath );

                    daemonGeneratorService.generateDaemon( request );
                }
            }
        }
        catch ( DaemonGeneratorException e )
        {
            throw new MojoExecutionException( "Error while generating daemon.", e );
        }
    }

    private org.codehaus.mojo.appassembler.model.JvmSettings convertJvmSettings( JvmSettings jvmSettings )
    {
        org.codehaus.mojo.appassembler.model.JvmSettings modelJvmSettings =
            new org.codehaus.mojo.appassembler.model.JvmSettings();

        modelJvmSettings.setInitialMemorySize( jvmSettings.getInitialMemorySize() );
        modelJvmSettings.setMaxMemorySize( jvmSettings.getMaxMemorySize() );
        modelJvmSettings.setMaxStackSize( jvmSettings.getMaxStackSize() );
        if ( jvmSettings.getSystemProperties() == null )
        {
            modelJvmSettings.setSystemProperties( new ArrayList() );
        }
        else
        {
            modelJvmSettings.setSystemProperties( Arrays.asList( jvmSettings.getSystemProperties() ) );
        }
        if ( jvmSettings.getExtraArguments() == null )
        {
            modelJvmSettings.setExtraArguments( new ArrayList() );
        }
        else
        {
            modelJvmSettings.setExtraArguments( Arrays.asList( jvmSettings.getExtraArguments() ) );
        }

        return modelJvmSettings;
    }
}
