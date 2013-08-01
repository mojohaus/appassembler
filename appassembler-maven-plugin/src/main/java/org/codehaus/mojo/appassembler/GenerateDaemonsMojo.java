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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Generates JSW based daemon wrappers.
 * 
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal generate-daemons
 * @requiresDependencyResolution runtime
 * @phase package
 * @threadSafe
 */
public class GenerateDaemonsMojo
    extends AbstractScriptGeneratorMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * The base directory of the project.
     * 
     * @parameter expression="${basedir}"
     * @required
     */
    private File basedir;

    /**
     * The name of the target directory for configuration files.
     *
     * @parameter default-value="conf"
     * @since 1.5
     * @todo Synchronize the default value with the other mojos in 2.0
     */
    private String configurationDirectory;

    /**
     * Set of {@linkplain Daemon}s to generate.
     * 
     * @parameter
     * @required
     */
    private Set daemons;

    /**
     * {@linkplain JvmSettings} describing min/max memory and stack size, system properties and extra arguments.
     * 
     * @parameter
     */
    private JvmSettings defaultJvmSettings;

    /**
     * Path (relative to <code>assembleDirectory</code>) of the desired output
     * repository.
     *
     * @parameter default-value="lib"
     * @since 1.5
     * @todo Synchronize the default value with the other mojos in 2.0
     */
    private String repositoryName;

    /**
     * Target directory for generated daemons.
     * 
     * @parameter default-value="${project.build.directory}/generated-resources/appassembler"
     * @required
     */
    private File target;

    /**
     * When enable, name wrapper configuration file as wrapper-${daemon.id}.conf
     * 
     * @parameter default-value="false"
     * @since 1.3
     */
    private boolean useDaemonIdAsWrapperConfName;

    /**
     * Use this option to override the current built-in delta pack binary. You will need to unpack your delta pack version 
     * to a known location set by this option
     * 
     * @since 1.4.0
     * @parameter expression="${externalDeltaPackDirectory}"
     */
    private File externalDeltaPackDirectory;

    // -----------------------------------------------------------------------
    // AbstractMojo Implementation
    // -----------------------------------------------------------------------

    /**
     * calling from Maven.
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        if ( isUseWildcardClassPath() && !repositoryLayout.equalsIgnoreCase( "flat" ) )
        {
            throw new MojoExecutionException( "The useWildcardClassPath works only in"
                + " combination with repositoryLayout flat." );
        }

        for ( Iterator itd = daemons.iterator(); itd.hasNext(); )
        {
            Daemon daemon = (Daemon) itd.next();

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

            ArtifactRepositoryLayout artifactRepositoryLayout = getArtifactRepositoryLayout();

            // -----------------------------------------------------------------------
            // Create a daemon object from the POM configuration
            // -----------------------------------------------------------------------

            org.codehaus.mojo.appassembler.model.Daemon modelDaemon = convertDaemon( daemon, modelJvmSettings );

            // -----------------------------------------------------------------------
            // Default Handling for license file
            // -----------------------------------------------------------------------
            if ( this.licenseHeaderFile != null )
            {
                // Allow overwrite if not set otherwise the set license header file will be used.
                if ( modelDaemon.getLicenseHeaderFile() == null )
                {
                    modelDaemon.setLicenseHeaderFile( this.licenseHeaderFile.toString() );
                }
            }

            modelDaemon.setConfigurationDirectory( configurationDirectory );
            modelDaemon.setEnvironmentSetupFileName( environmentSetupFileName );
            modelDaemon.setRepositoryName( repositoryName );
            modelDaemon.setUseTimestampInSnapshotFileName( useTimestampInSnapshotFileName );
            modelDaemon.setUseDaemonIdAsWrapperConfName( useDaemonIdAsWrapperConfName );
            modelDaemon.setUseWildcardClassPath( isUseWildcardClassPath() );

            if ( this.unixScriptTemplate != null )
            {
                modelDaemon.setUnixScriptTemplate( unixScriptTemplate );
            }
            if ( this.windowsScriptTemplate != null )
            {
                modelDaemon.setWindowsScriptTemplate( windowsScriptTemplate );
            }

            if ( this.externalDeltaPackDirectory != null )
            {
                modelDaemon.setExternalDeltaPackDirectory( this.externalDeltaPackDirectory.getAbsolutePath() );
            }
            
            // -----------------------------------------------------------------------
            //
            // -----------------------------------------------------------------------

            for ( Iterator i = daemon.getPlatforms().iterator(); i.hasNext(); )
            {
                String platform = (String) i.next();

                File output = new File( target, platform );

                DaemonGenerationRequest request = new DaemonGenerationRequest();

                // TODO: split platform from generator (platform = operating systems, generator = jsw, booter,
                // standard). Generator is a property of the daemon itself
                request.setPlatform( platform );
                request.setStubDescriptor( descriptor );
                request.setStubDaemon( modelDaemon );
                request.setOutputDirectory( output );
                request.setMavenProject( mavenProject );
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

                File outputDirectory = new File( request.getOutputDirectory(), daemon.getId() );

                // @todo Refactor: This code is near identical to what is in AssembleMojo
                // The repo where the jar files will be installed
                ArtifactRepository artifactRepository =
                    artifactRepositoryFactory.createDeploymentArtifactRepository( "appassembler", "file://"
                        + outputDirectory.getAbsolutePath() + "/" + repositoryName, artifactRepositoryLayout, false );

                for ( Iterator it = artifacts.iterator(); it.hasNext(); )
                {
                    Artifact artifact = (Artifact) it.next();

                    installArtifact( artifact, artifactRepository, this.useTimestampInSnapshotFileName );
                }

                // install the project's artifact in the new repository
                installArtifact( projectArtifact, artifactRepository );

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
        modelDaemon.setConfigurationDirectory( daemon.getConfigurationDirectory() );
        modelDaemon.setLicenseHeaderFile( daemon.getLicenseHeaderFile() );
        modelDaemon.setShowConsoleWindow( daemon.isShowConsoleWindow() );
        modelDaemon.setEnvironmentSetupFileName( daemon.getEnvironmentSetupFileName() );
        modelDaemon.setRepositoryName( daemon.getRepositoryName() );

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
            modelDaemon.setGeneratorConfigurations( convertGeneratorConfigurations( daemon.getGeneratorConfigurations() ) );
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

    private org.codehaus.mojo.appassembler.model.GeneratorConfiguration convertGeneratorConfiguration( GeneratorConfiguration config )
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

    /**
     * Seth the daemon.
     * 
     * @param daemons
     */
    public void setDaemons( Set daemons )
    {
        this.daemons = daemons;
    }
}
