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
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Generates JSW based daemon wrappers.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Mojo( name = "generate-daemons", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true )
public class GenerateDaemonsMojo
    extends AbstractScriptGeneratorMojo
{

    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * The base directory of the project.
     */
    @Parameter( defaultValue = "${basedir}", required = true )
    private File basedir;

    /**
     * Set of {@linkplain Daemon}s to generate.
     */
    @Parameter( required = true )
    private Set<Daemon> daemons;

    /**
     * {@linkplain JvmSettings} describing min/max memory and stack size, system properties and extra arguments.
     */
    @Parameter
    private JvmSettings defaultJvmSettings;

    /**
     * Path (relative to <code>assembleDirectory</code>) of the desired output repository.
     *
     * @since 1.5
     * @todo Synchronize the default value with the other mojos in 2.0
     */
    @Parameter( defaultValue = "lib" )
    private String repositoryName;

    /**
     * Target directory for generated daemons.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-resources/appassembler", required = true )
    private File target;

    /**
     * When set to true different platforms
     * will be output to their own directory (named by platform)
     * within the target directory.
     * When set to false, all platforms are output to the same target directory.
     *
     * @Since 2.0.0
     */
    @Parameter(defaultValue = "true")
    private boolean separateTargetPlatforms;

    /**
     * When enable, name wrapper configuration file as wrapper-${daemon.id}.conf
     *
     * @since 1.3
     */
    @Parameter( defaultValue = "false" )
    private boolean useDaemonIdAsWrapperConfName;

    /**
     * When enable, prefix the wrapper executable as ${daemon.id}. Otherwise, use the original name( ie wrapper )
     *
     * @since 1.8
     */
    @Parameter( defaultValue = "false" )
    private boolean useDaemonIdAsWrapperExePrefixName;

    /**
     * Use this option to override the current built-in delta pack binary. You will need to unpack your delta pack
     * version to a known location set by this option
     *
     * @since 1.4.0
     */
    @Parameter( property = "externalDeltaPackDirectory" )
    private File externalDeltaPackDirectory;

    /**
     * Use this option to pre insert a content of a known file into the generated wrapper config file. For example:
     * $include ../conf/another-wrapper.conf
     *
     * @since 1.7.0
     */
    @Parameter( property = "preWrapperConf" )
    private File preWrapperConf;

    /**
     * Set the name of the generated script name in bin folder instead
     * of using {@link Daemon#getId()}.
     *
     */
    @Parameter
    private String binFileName;

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

        if ( useWildcardClassPath && !repositoryLayout.equalsIgnoreCase( "flat" ) )
        {
            throw new MojoExecutionException( "The useWildcardClassPath works only in"
                + " combination with repositoryLayout flat." );
        }

        if ( preClean )
        {
            removeDirectory( target );
        }

        for ( Daemon daemon : daemons )
        {
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
            if ( this.licenseHeaderFile != null && modelDaemon.getLicenseHeaderFile() == null)
            {
                // Allow overwrite if not set otherwise the set license header file will be used.
                modelDaemon.setLicenseHeaderFile( this.licenseHeaderFile.toString() );
            }

            modelDaemon.setConfigurationDirectory( configurationDirectory );
            modelDaemon.setEnvironmentSetupFileName( environmentSetupFileName );
            modelDaemon.setRepositoryName( repositoryName );
            modelDaemon.setUseTimestampInSnapshotFileName( useTimestampInSnapshotFileName );
            modelDaemon.setUseDaemonIdAsWrapperConfName( useDaemonIdAsWrapperConfName );
            modelDaemon.setUseDaemonIdAsWrapperExePrefixName( useDaemonIdAsWrapperExePrefixName );
            modelDaemon.setUseWildcardClassPath( useWildcardClassPath );

            if ( endorsedDir != null )
            {
                modelDaemon.setEndorsedDir( endorsedDir );
            }

            if ( preWrapperConf != null )
            {
                modelDaemon.setPreWrapperConf( preWrapperConf.getAbsolutePath() );
            }

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

            for ( String platform : daemon.getPlatforms() )
            {
                final File output;
                if  ( separateTargetPlatforms ) {
                    output = new File(target, platform);
                } else {
                    output = target;
                }

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
                request.setOutputFileNameMapping( this.outputFileNameMapping );
                request.setBinScriptName( binFileName );

                try
                {
                    daemonGeneratorService.generateDaemon( request );
                }
                catch ( DaemonGeneratorException e )
                {
                    throw new MojoExecutionException( "Error while generating daemon.", e );
                }

                File outputDirectory = new File( request.getOutputDirectory(), daemon.getId() );

                // ----------------------------------------------------------------------
                // Install dependencies in the new repository
                // ----------------------------------------------------------------------
                super.installDependencies( outputDirectory.getAbsolutePath(), repositoryName );

                // ----------------------------------------------------------------------
                // Copy configuration directory
                // ----------------------------------------------------------------------

                if ( this.preAssembleDirectory != null && this.preAssembleDirectory.isDirectory() )
                {
                    doCopyPreAssembleDirectory( outputDirectory.getAbsolutePath() );
                }

                if ( this.copyConfigurationDirectory && configurationSourceDirectory.isDirectory() )
                {
                    doCopyConfigurationDirectory( outputDirectory.getAbsolutePath() );
                }

                // ----------------------------------------------------------------------
                // Create logs and temp dirs if specified
                // ----------------------------------------------------------------------

                doCreateExtraDirectories( outputDirectory );
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
        modelDaemon.setWrapperMainClass( daemon.getWrapperMainClass() );
        modelDaemon.setWrapperLogFile( daemon.getWrapperLogFile() );
        modelDaemon.setCommandLineArguments( daemon.getCommandLineArguments() );
        modelDaemon.setConfigurationDirectory( daemon.getConfigurationDirectory() );
        modelDaemon.setLicenseHeaderFile( daemon.getLicenseHeaderFile() );
        modelDaemon.setShowConsoleWindow( daemon.isShowConsoleWindow() );
        modelDaemon.setEnvironmentSetupFileName( daemon.getEnvironmentSetupFileName() );
        modelDaemon.setRepositoryName( daemon.getRepositoryName() );
        modelDaemon.setEndorsedDir( daemon.getEndorsedDir() );
        modelDaemon.setPreWrapperConf( daemon.getPreWrapperConf() );
        modelDaemon.setName( daemon.getName() );
        modelDaemon.setLongName( daemon.getLongName() );

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

    private List<org.codehaus.mojo.appassembler.model.GeneratorConfiguration> convertGeneratorConfigurations( List<GeneratorConfiguration> generatorConfigurations )
    {
        List<org.codehaus.mojo.appassembler.model.GeneratorConfiguration> value =
            new ArrayList<org.codehaus.mojo.appassembler.model.GeneratorConfiguration>( generatorConfigurations.size() );
        for ( GeneratorConfiguration config : generatorConfigurations )
        {
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
            modelJvmSettings.setSystemProperties( new ArrayList<String>() );
        }
        else
        {
            modelJvmSettings.setSystemProperties( Arrays.asList( jvmSettings.getSystemProperties() ) );
        }
        if ( jvmSettings.getExtraArguments() == null )
        {
            modelJvmSettings.setExtraArguments( new ArrayList<String>() );
        }
        else
        {
            modelJvmSettings.setExtraArguments( Arrays.asList( jvmSettings.getExtraArguments() ) );
        }

        return modelJvmSettings;
    }

}
