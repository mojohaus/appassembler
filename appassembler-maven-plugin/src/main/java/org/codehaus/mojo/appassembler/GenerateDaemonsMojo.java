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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService;
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
    extends AbstractAppAssemblerMojo
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
     * Setup file in $BASEDIR/bin to be called prior to execution. If this optional environment file also sets up
     * WRAPPER_CONF_OVERRIDES variable, it will be passed into JSW native launcher's command line arguments to override
     * wrapper.conf's properties. See http://wrapper.tanukisoftware.com/doc/english/props-command-line.html for details.
     * 
     * @parameter
     * @since 1.2.3
     */
    private String environmentSetupFileName;

    /**
     * You can define a license header file which will be used instead the default header in the generated scripts.
     * 
     * @parameter
     * @since 1.2
     */
    private File licenseHeaderFile;

    /**
     * Target directory for generated daemons.
     * 
     * @parameter expression="${project.build.directory}/generated-resources/appassembler"
     * @required
     */
    private File target;

    /**
     * The unix template of the generated script. It can be a file or resource path. If not given, an internal one is
     * used. Use with care since it not guaranteed to be compatible with future plugin releases.
     * 
     * @since 1.3
     * @parameter expression="${unixScriptTemplate}"
     */
    private String unixScriptTemplate;

    /**
     * When enable, name wrapper configuration file as wrapper-${daemon.id}.conf
     * 
     * @parameter default-value="false"
     * @since 1.3
     */
    private boolean useDaemonIdAsWrapperConfName;

    /**
     * Sometimes it happens that you have many dependencies which means in other words having a very long classpath. And
     * sometimes the classpath becomes too long (in particular on Windows based platforms). This option can help in such
     * situation. If you activate that your classpath contains only a <a href=
     * "http://docs.oracle.com/javase/6/docs/technotes/tools/windows/classpath.html" >classpath wildcard</a> (REPO/*).
     * But be aware that this works only in combination with Java 1.6 and above and with {@link #repositoryLayout}
     * <code>flat</code>. Otherwise this configuration will not work.
     * 
     * @since 1.3.1
     * @parameter default-value="false"
     */
    private boolean useWildcardClassPath;

    /**
     * The windows template of the generated script. It can be a file or resource path. If not given, an internal one is
     * used. Use with care since it is not guaranteed to be compatible with future plugin releases.
     * 
     * @since 1.3
     * @parameter expression="${windowsScriptTemplate}"
     */
    private String windowsScriptTemplate;

    // -----------------------------------------------------------------------
    // Read-only parameters
    // -----------------------------------------------------------------------

    /**
     * @readonly
     * @parameter expression="${project.runtimeArtifacts}"
     */
    private List artifacts;

    /**
     * The maven project in question.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /**
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map availableRepositoryLayouts;

    /**
     * @component
     */
    private DaemonGeneratorService daemonGeneratorService;

    // -----------------------------------------------------------------------
    // AbstractMojo Implementation
    // -----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        if ( isUseWildcardClassPath() && !repositoryLayout.equalsIgnoreCase( "flat" ) )
        {
            throw new MojoExecutionException(
                                              "The useWildcardClassPath works only in combination with repositoryLayout flat." );
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

            modelDaemon.setEnvironmentSetupFileName( environmentSetupFileName );
            modelDaemon.setUseTimestampInSnapshotFileName( useTimestampInSnapshotFileName );
            modelDaemon.setUseDaemonIdAsWrapperConfName( useDaemonIdAsWrapperConfName );
            modelDaemon.setUseWildcardClassPath( useWildcardClassPath );

            if ( this.unixScriptTemplate != null )
            {
                modelDaemon.setUnixScriptTemplate( unixScriptTemplate );
            }
            if ( this.windowsScriptTemplate != null )
            {
                modelDaemon.setWindowsScriptTemplate( windowsScriptTemplate );
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

                File outputDirectory = new File( request.getOutputDirectory(), daemon.getId() );

                // The repo where the jar files will be installed
                // FIXME: /lib hard coded. Should be made configurable.
                // via repositoryName like in AssembleMojo ?
                // Might be refactored into AbstractAppAssemblerMojo?
                ArtifactRepository artifactRepository =
                    artifactRepositoryFactory.createDeploymentArtifactRepository( "appassembler", "file://"
                        + outputDirectory.getAbsolutePath() + "/lib", artifactRepositoryLayout, false );

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

    public void setAvailableRepositoryLayouts( Map availableRepositoryLayouts )
    {
        this.availableRepositoryLayouts = availableRepositoryLayouts;
    }

    public void setDaemons( Set daemons )
    {
        this.daemons = daemons;
    }

    /**
     * Should the <code>/*</code> part for the classpath be used or not.
     * 
     * @return true if the wild card class path will be used false otherwise.
     */
    public boolean isUseWildcardClassPath()
    {
        return useWildcardClassPath;
    }

    /**
     * Use wildcard classpath or not.
     * 
     * @param useWildcardClassPath true to use wildcard classpath false otherwise.
     */
    public void setUseWildcardClassPath( boolean useWildcardClassPath )
    {
        this.useWildcardClassPath = useWildcardClassPath;
    }

}
