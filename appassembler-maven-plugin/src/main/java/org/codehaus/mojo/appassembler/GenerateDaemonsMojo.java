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
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates JSW based daemon wrappers.
 * 
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal generate-daemons
 * @requiresDependencyResolution runtime
 * @phase package
 */
public class GenerateDaemonsMojo
    extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * Set of {@linkplain Daemon}s to generate.
     * @parameter
     * @required
     */
    private Set daemons;

    /**
     * {@linkplain JvmSettings} describing min/max memory and stack size, system properties and extra arguments.
     * @parameter
     */
    private JvmSettings defaultJvmSettings;

    /**
     * The base directory of the project.
     * @parameter expression="${basedir}"
     * @required
     */
    private File basedir;

    /**
     * Target directory for generated daemons.
     * @parameter expression="${project.build.directory}/generated-resources/appassembler"
     * @required
     */
    private File target;

    /**
     * The maven project in question.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

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
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /**
     * @component
     */
    private DaemonGeneratorService daemonGeneratorService;

    /**
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map availableRepositoryLayouts;

    // -----------------------------------------------------------------------
    // AbstractMojo Implementation
    // -----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException, MojoFailureException
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

            ArtifactRepositoryLayout artifactRepositoryLayout =
                (ArtifactRepositoryLayout) availableRepositoryLayouts.get( repositoryLayout );
            if ( artifactRepositoryLayout == null )
            {
                throw new MojoFailureException( "Unknown repository layout '" + repositoryLayout + "'." );
            }

            // -----------------------------------------------------------------------
            // Create a daemon object from the POM configuration
            // -----------------------------------------------------------------------

            org.codehaus.mojo.appassembler.model.Daemon modelDaemon = convertDaemon( daemon, modelJvmSettings );

            // -----------------------------------------------------------------------
            //
            // -----------------------------------------------------------------------

            for ( Iterator i = daemon.getPlatforms().iterator(); i.hasNext(); )
            {
                String platform = (String) i.next();

                File output = new File( target, platform );

                DaemonGenerationRequest request = new DaemonGenerationRequest();

                // TODO: split platform from generator (platform = operating systems, generator = jsw, booter, standard). Generator is a property of the daemon itself
                request.setPlatform( platform );
                request.setStubDescriptor( descriptor );
                request.setStubDaemon( modelDaemon );
                request.setOutputDirectory( output );
                request.setMavenProject( project );
                request.setLocalRepository( localRepository );
                request.setRepositoryLayout( artifactRepositoryLayout );

                try
                {
                    daemonGeneratorService.generateDaemon( request );
                }
                catch ( DaemonGeneratorException e )
                {
                    throw new MojoExecutionException( "Error while generating daemon.", e );
                }
            }
        }
    }

    // TODO: see if it is possible to just inherit from the model daemon
    private org.codehaus.mojo.appassembler.model.Daemon convertDaemon( Daemon daemon,
                                                                       org.codehaus.mojo.appassembler.model.JvmSettings modelJvmSettings )
    {
        org.codehaus.mojo.appassembler.model.Daemon modelDaemon;

        modelDaemon = new org.codehaus.mojo.appassembler.model.Daemon();

        modelDaemon.setId( daemon.getId() );
        modelDaemon.setMainClass( daemon.getMainClass() );
        modelDaemon.setCommandLineArguments( daemon.getCommandLineArguments() );
        modelDaemon.setShowConsoleWindow( daemon.isShowConsoleWindow() );
        modelDaemon.setEnvironmentSetupFileName( daemon.getEnvironmentSetupFileName() );

        if ( daemon.getJvmSettings() != null )
        {
            modelDaemon.setJvmSettings( convertJvmSettings( daemon.getJvmSettings() ) );
        }
        else
        {
            modelDaemon.setJvmSettings( modelJvmSettings );
        }

        if ( daemon.getGeneratorConfigurations() != null )
        {
            modelDaemon.setGeneratorConfigurations(
                convertGeneratorConfigurations( daemon.getGeneratorConfigurations() ) );
        }

        return modelDaemon;
    }

    private List convertGeneratorConfigurations( List generatorConfigurations )
    {
        List value = new ArrayList( generatorConfigurations.size() );
        for ( Iterator i = generatorConfigurations.iterator(); i.hasNext(); )
        {
            GeneratorConfiguration config = (GeneratorConfiguration) i.next();

            value.add( convertGeneratorConfiguration( config ) );
        }
        return value;
    }

    private org.codehaus.mojo.appassembler.model.GeneratorConfiguration convertGeneratorConfiguration(
        GeneratorConfiguration config )
    {
        org.codehaus.mojo.appassembler.model.GeneratorConfiguration value =
            new org.codehaus.mojo.appassembler.model.GeneratorConfiguration();
        value.setGenerator( config.getGenerator() );
        value.setConfiguration( config.getConfiguration() );
        value.setIncludes( config.getIncludes() );
        
        return value;
    }

    // TODO: see if it is possible to just inherit from the model JVM Settings
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

    public void setAvailableRepositoryLayouts( Map availableRepositoryLayouts )
    {
        this.availableRepositoryLayouts = availableRepositoryLayouts;
    }

    public void setDaemons( Set daemons )
    {
        this.daemons = daemons;
    }
}
