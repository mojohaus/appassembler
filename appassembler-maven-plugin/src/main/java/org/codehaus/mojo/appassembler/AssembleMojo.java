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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.script.Platform;
import org.codehaus.mojo.appassembler.model.Classpath;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.mojo.appassembler.util.DependencyFactory;
import org.codehaus.plexus.util.StringUtils;

// @deprecated Use the generate-daemons goal instead

/**
 * Assembles the artifacts and generates bin scripts for the configured applications
 *
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 */
@Mojo( name = "assemble", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true )
public class AssembleMojo
    extends AbstractScriptGeneratorMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * The directory that will be used to assemble the artifacts in and place the bin scripts.
     */
    @Parameter( property = "assembleDirectory", defaultValue = "${project.build.directory}/appassembler", required = true )
    private File assembleDirectory;

    /**
     * The file extensions to use for bin files. The file extensions are stored in a Map that uses the platform name as
     * key. To change the file extension for Unix bin files to ".sh" use this configuration:
     *
     * <pre>
     *          &lt;binFileExtensions&gt;
     *            &lt;unix&gt;.sh&lt;/unix&gt;
     *          &lt;/binFileExtensions&gt;
     * </pre>
     *
     * @since 1.1
     */
    @Parameter
    protected Map/* <String, String> */binFileExtensions;

    /**
     * Define the name of binary folder.
     *
     * @since 1.2
     */
    @Parameter( defaultValue = "bin" )
    private String binFolder;

    /**
     * Extra arguments that will be given to the JVM verbatim. If you define JvmSettings on the
     * {@link Program#setJvmSettings(JvmSettings)} level this part will be overwritten by the given parameters on
     * program level. Otherwise if {@link Program#setJvmSettings(JvmSettings)} is not given these settings will be used
     * instead. This can be used to define some default values whereas by using the
     * {@link Program#setJvmSettings(JvmSettings)} to overwrite the default settings. This is only valid for the
     * extraJvmArguments not for the rest of the {@link JvmSettings#}. Since 1.2 it's possible to use place holders
     * <code>@BASEDIR@</code> and <code>@REPO@</code> which will be expanded based on the platform for which the
     * appropriate scripts will be generated.
     */
    @Parameter
    private String extraJvmArguments;

    /**
     * If the <code>configurationDirectory</code> (<code>etc</code> by default) should be included in the beginning of
     * the classpath in the generated bin files.
     */
    @Parameter( defaultValue = "true" )
    private boolean includeConfigurationDirectoryInClasspath;

    /**
     * The default platforms the plugin will generate bin files for. Configure with string values - "all"(default/empty)
     * | "windows" | "unix".
     */
    @Parameter
    private Set platforms;

    /**
     * The set of Programs that bin files will be generated for.
     */
    @Parameter( required = true )
    private Set programs;

    /**
     * This can be used to put the project artifact as the first entry in the classpath after the configuration folder (
     * <code>etc</code> by default). The default behavior is to have the project artifact at the last position in
     * classpath.
     *
     * @since 1.2.1
     */
    @Parameter( defaultValue = "false" )
    private boolean projectArtifactFirstInClassPath;

    /**
     * Path (relative to <code>assembleDirectory</code>) of the desired output repository.
     */
    @Parameter( defaultValue = "repo" )
    private String repositoryName;

    /**
     * Show console window when execute this application. When false, the generated java command runs in background.
     * This works best for Swing application where the command line invocation is not blocked.
     */
    @Parameter( defaultValue = "true" )
    private boolean showConsoleWindow;

    /**
     * The following can be used to use all project dependencies instead of the default behavior which represents
     * <code>runtime</code> dependencies only.
     *
     * @since 1.2.3
     */
    @Parameter( defaultValue = "false" )
    private boolean useAllProjectDependencies;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // CONSTANTS
    // ----------------------------------------------------------------------

    private static final Set VALID_PLATFORMS = Collections.unmodifiableSet( new HashSet( Arrays.asList( new String[] {
        "unix", "windows" } ) ) );

    // ----------------------------------------------------------------------
    // Validate
    // ----------------------------------------------------------------------

    private void validate( Set defaultPlatforms )
        throws MojoFailureException, MojoExecutionException
    {
        // ----------------------------------------------------------------------
        // Validate Programs
        // ----------------------------------------------------------------------

        ArrayList programNames = new ArrayList();

        for ( Iterator i = programs.iterator(); i.hasNext(); )
        {
            Program program = (Program) i.next();

            if ( program.getName() != null )
            {
                program.setId( program.getName() );
                getLog().warn( "The usage of program name (" + program.getName()
                                   + ") is deprecated. Please use program.id instead." );
            }

            if ( program.getMainClass() == null || program.getMainClass().trim().equals( "" ) )
            {
                throw new MojoFailureException( "Missing main class in Program configuration" );
            }

            // FIXME: After migration to Java 1.5 the following check could be
            // done simpler!
            if ( !programNames.contains( program.getId() ) )
            {
                programNames.add( program.getId() );
            }
            else
            {
                throw new MojoFailureException( "The program id: " + program.getId() + " exists more than once!" );
            }

            // platforms
            program.setPlatforms( validatePlatforms( program.getPlatforms(), defaultPlatforms ) );
        }

    }

    // ----------------------------------------------------------------------
    // Execute
    // ----------------------------------------------------------------------

    public void checkDeprecatedParameterAndFailIfOneOfThemIsUsed()
        throws MojoExecutionException
    {
    }

    /**
     * calling from Maven.
     *
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     * @throws {@link MojoExecutionException}
     * @throws {@link MojoFailureException}
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Set defaultPlatforms = validatePlatforms( platforms, VALID_PLATFORMS );

        checkDeprecatedParameterAndFailIfOneOfThemIsUsed();

        // validate input and set defaults
        validate( defaultPlatforms );

        if ( useWildcardClassPath && !repositoryLayout.equalsIgnoreCase( "flat" ) )
        {
            throw new MojoExecutionException( "useWildcardClassPath works only in "
                + "combination with repositoryLayout flat." );
        }

        // Set the extensions for bin files for the different platforms
        setBinFileExtensions();

        ArtifactRepositoryLayout artifactRepositoryLayout = getArtifactRepositoryLayout();

        if ( useAllProjectDependencies )
        {
            // TODO: This should be made different. We have to think about using
            // a default ArtifactFilter
            Set dependencyArtifacts = mavenProject.getDependencyArtifacts();
            artifacts = new ArrayList();
            for ( Iterator it = dependencyArtifacts.iterator(); it.hasNext(); )
            {
                Artifact artifact = (Artifact) it.next();
                artifacts.add( artifact );
            }
        }

        // ----------------------------------------------------------------------
        // Install dependencies in the new repository
        // ----------------------------------------------------------------------
        super.installDependencies( assembleDirectory.getAbsolutePath(), repositoryName );

        // ----------------------------------------------------------------------
        // Setup
        // ----------------------------------------------------------------------

        setUpWorkingArea();

        // ----------------------------------------------------------------------
        // Create bin files
        // ----------------------------------------------------------------------

        for ( Iterator it = programs.iterator(); it.hasNext(); )
        {
            Program program = (Program) it.next();

            if ( program.getName() != null )
            {
                program.setId( program.getName() );
            }

            Set validatedPlatforms = validatePlatforms( program.getPlatforms(), defaultPlatforms );

            for ( Iterator platformIt = validatedPlatforms.iterator(); platformIt.hasNext(); )
            {
                String platform = (String) platformIt.next();

                // TODO: seems like a bug in the generator that the request is
                // modified
                org.codehaus.mojo.appassembler.model.Daemon daemon =
                    programToDaemon( program, artifactRepositoryLayout );
                DaemonGenerationRequest request =
                    new DaemonGenerationRequest( daemon, mavenProject, localRepository, assembleDirectory, binFolder );
                request.setStubDaemon( request.getDaemon() );

                request.setPlatform( platform );

                try
                {
                    daemonGeneratorService.generateDaemon( request );
                }
                catch ( DaemonGeneratorException e )
                {
                    throw new MojoExecutionException( "Error while generating script for the program '"
                        + program.getId() + "' for the platform '" + platform + "': " + e.getMessage(), e );
                }
            }
        }

        // ----------------------------------------------------------------------
        // Copy configuration directory
        // ----------------------------------------------------------------------

        if ( this.copyConfigurationDirectory )
        {
            doCopyConfigurationDirectory( assembleDirectory.getAbsolutePath() );
        }
    }

    private org.codehaus.mojo.appassembler.model.Daemon programToDaemon( Program program,
                                                                         ArtifactRepositoryLayout artifactRepositoryLayout )
    {
        org.codehaus.mojo.appassembler.model.Daemon daemon = new org.codehaus.mojo.appassembler.model.Daemon();

        if ( program.getName() == null )
        {
            daemon.setId( program.getId() );
        }
        else
        {
            daemon.setId( program.getName() );
        }
        daemon.setMainClass( program.getMainClass() );
        daemon.setShowConsoleWindow( showConsoleWindow );
        daemon.setCommandLineArguments( program.getCommandLineArguments() );

        if ( program.getLicenseHeaderFile() != null )
        {
            getLog().debug( "Using the program specific license header. :" + program.getLicenseHeaderFile() );
            daemon.setLicenseHeaderFile( program.getLicenseHeaderFile().getPath() );
        }
        else
        {
            getLog().debug( "Using the global defined license header. :" + licenseHeaderFile );

            if ( licenseHeaderFile != null )
            {
                daemon.setLicenseHeaderFile( this.licenseHeaderFile.getAbsolutePath() );
            }
            else
            {
                daemon.setLicenseHeaderFile( null );
            }
        }

        List directories = new ArrayList();

        if ( includeConfigurationDirectoryInClasspath )
        {
            Directory directory = new Directory();
            directory.setRelativePath( configurationDirectory );
            directories.add( directory );
        }

        if ( daemon.getClasspath() == null )
        {
            daemon.setClasspath( new Classpath() );
        }

        daemon.getClasspath().setDirectories( directories );

        daemon.setRepositoryName( repositoryName );

        daemon.setEndorsedDir( endorsedDir );

        List dependencies = new ArrayList();

        // TODO: This should be done in a more elegant way for 2.0
        // TODO: Check if the classpath wildcard could be used for Daemons as well?

        if ( useWildcardClassPath )
        {
            Dependency dependency = new Dependency();
            dependency.setGroupId( "" );
            dependency.setArtifactId( "" );
            dependency.setVersion( "" );
            dependency.setRelativePath( "*" );
            dependencies.add( dependency );
        }
        else
        {
            List classPathArtifacts = new ArrayList();

            if ( projectArtifactFirstInClassPath )
            {
                classPathArtifacts.add( projectArtifact );
                classPathArtifacts.addAll( artifacts );
            }
            else
            {
                classPathArtifacts.addAll( artifacts );
                classPathArtifacts.add( projectArtifact );
            }

            for ( Iterator it = classPathArtifacts.iterator(); it.hasNext(); )
            {
                Artifact artifact = (Artifact) it.next();

                dependencies.add( DependencyFactory.create( artifact, artifactRepositoryLayout,
                                                            this.useTimestampInSnapshotFileName, outputFileNameMapping ) );
            }

        }

        daemon.getClasspath().setDependencies( dependencies );

        daemon.setJvmSettings( convertToJvmSettingsWithDefaultHandling( program ) );

        daemon.setEnvironmentSetupFileName( this.environmentSetupFileName );

        if ( this.unixScriptTemplate != null )
        {
            daemon.setUnixScriptTemplate( unixScriptTemplate );
        }
        if ( this.windowsScriptTemplate != null )
        {
            daemon.setWindowsScriptTemplate( windowsScriptTemplate );
        }

        return daemon;
    }

    private JvmSettings convertToJvmSettingsWithDefaultHandling( Program program )
    {
        JvmSettings jvmSettings = new JvmSettings();

        if ( program.getJvmSettings() != null )
        {
            // Some kind of settings done on per program base so they take
            // precendence.
            jvmSettings = program.getJvmSettings();
        }
        else
        {
            // No settings in the program done so we use the default behaviour
            if ( StringUtils.isNotBlank( this.extraJvmArguments ) )
            {
                jvmSettings.setExtraArguments( parseTokens( this.extraJvmArguments ) );
            }
        }

        return jvmSettings;
    }

    // ----------------------------------------------------------------------
    // Set up the assemble environment
    // ----------------------------------------------------------------------

    private void setUpWorkingArea()
        throws MojoFailureException
    {
        // create (if necessary) directory for bin files
        File binDir = new File( assembleDirectory.getAbsolutePath(), binFolder.toString() );

        if ( !binDir.exists() )
        {

            boolean success = binDir.mkdirs();

            if ( !success )
            {
                throw new MojoFailureException( "Failed to create directory for bin files." );
            }
        }
    }

    private Set validatePlatforms( Set platformsToValidate, Set defaultPlatforms )
        throws MojoFailureException
    {
        if ( platformsToValidate == null )
        {
            return defaultPlatforms;
        }

        if ( platformsToValidate.size() == 1 && platformsToValidate.iterator().next().equals( "all" ) )
        {
            return VALID_PLATFORMS;
        }

        if ( !VALID_PLATFORMS.containsAll( platformsToValidate ) )
        {
            throw new MojoFailureException( "Non-valid default platform declared, supported types are: "
                + VALID_PLATFORMS );
        }

        return platformsToValidate;
    }

    /**
     * This will tokenize the given argument or give the extraJvmArguments back if the given argument is empty.
     *
     * @param arg The argument to parse.
     * @return List of arguments.
     */
    public static List parseTokens( String arg )
    {
        List extraJvmArguments = new ArrayList();

        if ( StringUtils.isEmpty( arg ) )
        {
            return extraJvmArguments;
        }

        StringTokenizer tokenizer = new StringTokenizer( arg );

        String argument = null;

        while ( tokenizer.hasMoreTokens() )
        {
            String token = tokenizer.nextToken();

            if ( argument != null )
            {
                if ( token.length() == 0 )
                {
                    // ignore it
                    continue;
                }

                int length = token.length();

                if ( token.charAt( length - 1 ) == '\"' )
                {
                    extraJvmArguments.add( argument + " " + token.substring( 0, length - 1 ) );
                    argument = null;
                }
                else
                {
                    argument += " " + token;
                }
            }
            else
            {
                // If the token starts with a ", save it
                if ( token.charAt( 0 ) == '\"' )
                {
                    argument = token.substring( 1 );
                }
                else
                {
                    extraJvmArguments.add( token );
                }
            }
        }

        return extraJvmArguments;
    }

    /**
     * Set the extensions for bin files for the supported platforms. The values are taken from the Mojo's
     * <code>binFileExtensions</code> parameter.
     */
    private void setBinFileExtensions()
        throws MojoFailureException
    {
        if ( binFileExtensions != null )
        {
            Set keySet = binFileExtensions.keySet();
            Iterator iterator = keySet.iterator();
            while ( iterator.hasNext() )
            {
                String platformName = (String) iterator.next();
                if ( !VALID_PLATFORMS.contains( platformName ) )
                {
                    getLog().warn( "Bin file extension configured for a non-valid platform (" + platformName
                                       + "), supported platforms are: " + VALID_PLATFORMS );
                }
                else
                {
                    try
                    {
                        Platform platform = Platform.getInstance( platformName );
                        platform.setBinFileExtension( (String) binFileExtensions.get( platformName ) );
                    }
                    catch ( DaemonGeneratorException e )
                    {
                        getLog().warn( "Unable to set the bin file extension for " + platformName, e );
                    }
                }
            }
        }
    }

}
