/**
 * The MIT License
 * 
 * Copyright 2006-2011 The Codehaus.
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
package org.codehaus.mojo.appassembler;

import java.io.File;
import java.io.IOException;
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
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService;
import org.codehaus.mojo.appassembler.daemon.script.Platform;
import org.codehaus.mojo.appassembler.model.Classpath;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

// @deprecated Use the generate-daemons goal instead

/**
 * Assembles the artifacts and generates bin scripts for the configured
 * applications
 * 
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 * @goal assemble
 * @requiresDependencyResolution runtime
 * @phase package
 * @threadsafe
 */
public class AssembleMojo extends AbstractAppAssemblerMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * The directory that will be used to assemble the artifacts in and place
     * the bin scripts.
     * 
     * @required
     * @parameter expression="${assembleDirectory}"
     *            default-value="${project.build.directory}/appassembler"
     */
    private File assembleDirectory;

    /**
     * The set of Programs that bin files will be generated for.
     * 
     * @required
     * @parameter
     */
    private Set programs;

    /**
     * Define the name of binary folder.
     * 
     * @parameter default-value="bin"
     * @since 1.2
     */
    private String binFolder;

    /**
     * The name of the target directory for configuration files.
     * 
     * @parameter default-value="etc"
     */
    private String configurationDirectory;

    /**
     * The name of the source directory for configuration files.
     * 
     * @parameter default-value="src/main/config"
     * @since 1.1
     */
    private File configurationSourceDirectory;

    /**
     * If the source configuration directory should be copied to the configured <code>configurationDirectory</code>.
     * 
     * @parameter default-value="false"
     * @since 1.1
     */
    private boolean copyConfigurationDirectory;

    /**
     * If the <code>configurationDirectory</code> (<code>etc</code> by default)
     * should be included in the beginning of the classpath in the generated bin
     * files.
     * 
     * @parameter default-value="true"
     */
    private boolean includeConfigurationDirectoryInClasspath;

    /**
     * The layout of the generated Maven repository. Supported types - "default"
     * (Maven2) | "legacy" (Maven1) | "flat" (flat <code>lib/</code> style). The
     * style "legacy" is only supported if you are running under Maven 2.2.1 and
     * before.
     * 
     * @parameter default-value="default"
     */
    private String repositoryLayout;

    /**
     * Extra arguments that will be given to the JVM verbatim. If you define
     * JvmSettings on the {@link Program#setJvmSettings(JvmSettings)} level this
     * part will be overwritten by the given parameters on program level.
     * Otherwise if {@link Program#setJvmSettings(JvmSettings)} is not given
     * these settings will be used instead. This can be used to define some
     * default values whereas by using the {@link Program#setJvmSettings(JvmSettings)} to overwrite the default
     * settings. This is only valid for the extraJvmArguments not for the rest
     * of the {@link JvmSettings#}.
     * 
     * Since 1.2 it's possible to use place holder <code>@BASEDIR@</code> and <code>@REPO@</code> which will be expanded
     * based on the platform for
     * which the appropriate script will generated.
     * 
     * @parameter
     */
    private String extraJvmArguments;

    /**
     * The default platforms the plugin will generate bin files for. Configure
     * with string values - "all"(default/empty) | "windows" | "unix".
     * 
     * @parameter
     */
    private Set platforms;

    /**
     * Setup file in $BASEDIR/bin to be called prior to execution.
     * 
     * @parameter
     */
    private String environmentSetupFileName;

    /**
     * Set to false to skip repository generation.
     * 
     * @parameter default-value="true"
     */
    private boolean generateRepository;

    /**
     * Path (relative to assembleDirectory) of the desired output repository.
     * 
     * @parameter default-value="repo"
     */
    private String repositoryName;

    /**
     * This can be used to put the project artifact as the first entry in the
     * classpath after the configuration folder (<code>etc</code> by default).
     * The default behavior is to have the project artifact at the last position
     * in classpath.
     * 
     * @since 1.2.1
     * @parameter default-value="false"
     */
    private boolean projectArtifactFirstInClassPath;

    /**
     * The following can be used to use all dependencies instead of the default
     * behavior which represents runtime dependencies only.
     * 
     * @since 1.2.1
     * @parameter default-value="false"
     */
    private boolean useAllDependencies;

    /**
     * Sometimes it happens that you have many dependencies
     * which means in other words having a very long classpath. 
     * And sometimes the classpath becomes too long (in particular
     * on Windows based platforms). This option can help in such 
     * situation. If you activate that your classpath contains only a
     * <a href=
     * "http://docs.oracle.com/javase/6/docs/technotes/tools/windows/classpath.html"
     * >classpath wildcard</a> (REPO/*). But be aware that this works only in combination
     * with Java 1.6 and with {@link #repositoryLayout} <code>flat</code>.
     * Otherwise this configuration will not work.
     * 
     * @since 1.2.2
     * @parameter default-value="false"
     */
    private boolean useAsterikClassPath;

    /**
     * The file extensions to use for bin files. The file extensions are stored
     * in a Map that uses the platform name as key. To change the file extension
     * for Unix bin files to ".sh" use this configuration:
     * 
     * <pre>
     *          &lt;binFileExtensions&gt;
     *            &lt;unix&gt;.sh&lt;/unix&gt;
     *          &lt;/binFileExtensions&gt;
     * </pre>
     * 
     * @parameter
     * @since 1.1
     */
    protected Map/* <String, String> */binFileExtensions;

    // -----------------------------------------------------------------------
    // Read-only Parameters
    // -----------------------------------------------------------------------

    /**
     * @readonly
     * @parameter expression="${project}"
     */
    private MavenProject mavenProject;

    /**
     * @readonly
     * @parameter expression="${project.runtimeArtifacts}"
     */
    private List artifacts;

    /**
     * @readonly
     * @parameter expression="${project.artifact}"
     */
    private Artifact projectArtifact;

    /**
     * @readonly
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * Show console window when execute this application.
     * 
     * @parameter default-value="true"
     */
    private boolean showConsoleWindow;

    /**
     * You can define a license header file which will be used instead the
     * default header in the generated scripts.
     * 
     * @parameter
     * @since 1.2
     */
    private File licenseHeaderFile;

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
     * @component
     */
    private DaemonGeneratorService daemonGeneratorService;

    /**
     * @component role=
     *            "org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map availableRepositoryLayouts;

    // ----------------------------------------------------------------------
    // CONSTANTS
    // ----------------------------------------------------------------------

    private static final Set VALID_PLATFORMS = Collections
            .unmodifiableSet ( new HashSet ( Arrays.asList ( new String[] {
                    "unix",
                    "windows"
            } ) ) );

    // ----------------------------------------------------------------------
    // Validate
    // ----------------------------------------------------------------------

    private void validate ( Set defaultPlatforms ) throws MojoFailureException,
            MojoExecutionException
    {
        // ----------------------------------------------------------------------
        // Validate Programs
        // ----------------------------------------------------------------------

        ArrayList programNames = new ArrayList ( );

        for ( Iterator i = programs.iterator ( ); i.hasNext ( ); )
        {
            Program program = ( Program ) i.next ( );

            if ( program.getMainClass ( ) == null
                    || program.getMainClass ( ).trim ( ).equals ( "" ) )
            {
                throw new MojoFailureException (
                        "Missing main class in Program configuration" );
            }

            // FIXME: After migration to Java 1.5 the following check could be
            // done simpler!
            if ( !programNames.contains ( program.getName ( ) ) )
            {
                programNames.add ( program.getName ( ) );
            }
            else
            {
                throw new MojoFailureException ( "The program name: "
                        + program.getName ( ) + " exists more than once!" );
            }

            // platforms
            program.setPlatforms ( validatePlatforms ( program.getPlatforms ( ),
                    defaultPlatforms ) );
        }

    }

    // ----------------------------------------------------------------------
    // Execute
    // ----------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute () throws MojoExecutionException, MojoFailureException
    {
        Set defaultPlatforms = validatePlatforms ( platforms, VALID_PLATFORMS );

        // validate input and set defaults
        validate ( defaultPlatforms );

        if ( isUseAsterikClassPath ( )
                && !repositoryLayout.equalsIgnoreCase ( "flat" ) )
        {
            throw new MojoExecutionException (
                    "The useAsterikClassPath works only in combination with repositoryLayout flat." );
        }

        // Set the extensions for bin files for the different platforms
        setBinFileExtensions ( );

        ArtifactRepositoryLayout artifactRepositoryLayout = ( ArtifactRepositoryLayout ) availableRepositoryLayouts
                .get ( repositoryLayout );
        if ( artifactRepositoryLayout == null )
        {
            throw new MojoFailureException ( "Unknown repository layout '"
                    + repositoryLayout + "'." );
        }

        if ( isUseAllDependencies ( ) )
        {
            // TODO: This should be made different. We have to think about using
            // a default ArtifactFilter
            Set dependencyArtifacts = mavenProject.getDependencyArtifacts ( );
            artifacts = new ArrayList ( );
            for ( Iterator it = dependencyArtifacts.iterator ( ); it.hasNext ( ); )
            {
                Artifact artifact = ( Artifact ) it.next ( );
                artifacts.add ( artifact );
            }
        }

        // ----------------------------------------------------------------------
        // Install dependencies in the new repository
        // ----------------------------------------------------------------------
        if ( generateRepository )
        {
            // The repo where the jar files will be installed
            ArtifactRepository artifactRepository = artifactRepositoryFactory
                    .createDeploymentArtifactRepository ( "appassembler",
                            "file://" + assembleDirectory.getAbsolutePath ( )
                                    + "/" + repositoryName,
                            artifactRepositoryLayout, false );

            for ( Iterator it = artifacts.iterator ( ); it.hasNext ( ); )
            {
                Artifact artifact = ( Artifact ) it.next ( );

                installArtifact ( artifactRepository, artifact );
            }

            // install the project's artifact in the new repository
            installArtifact ( artifactRepository, projectArtifact );
        }

        // ----------------------------------------------------------------------
        // Setup
        // ----------------------------------------------------------------------

        setUpWorkingArea ( );

        // ----------------------------------------------------------------------
        // Create bin files
        // ----------------------------------------------------------------------

        for ( Iterator it = programs.iterator ( ); it.hasNext ( ); )
        {
            Program program = ( Program ) it.next ( );

            Set validatedPlatforms = validatePlatforms ( program.getPlatforms ( ),
                    defaultPlatforms );

            for ( Iterator platformIt = validatedPlatforms.iterator ( ); platformIt
                    .hasNext ( ); )
            {
                String platform = ( String ) platformIt.next ( );

                // TODO: seems like a bug in the generator that the request is
                // modified
                org.codehaus.mojo.appassembler.model.Daemon daemon = programToDaemon (
                        program, artifactRepositoryLayout );
                DaemonGenerationRequest request = new DaemonGenerationRequest (
                        daemon, mavenProject, localRepository,
                        assembleDirectory, binFolder );
                request.setStubDaemon ( request.getDaemon ( ) );

                request.setPlatform ( platform );

                try
                {
                    daemonGeneratorService.generateDaemon ( request );
                }
                catch ( DaemonGeneratorException e )
                {
                    throw new MojoExecutionException (
                            "Error while generating script for the program '"
                                    + program.getName ( )
                                    + "' for the platform '" + platform + "': "
                                    + e.getMessage ( ), e );
                }
            }
        }

        // ----------------------------------------------------------------------
        // Copy configuration directory
        // ----------------------------------------------------------------------

        if ( copyConfigurationDirectory )
        {
            copyConfigurationDirectory ( );
        }
    }

    private org.codehaus.mojo.appassembler.model.Daemon programToDaemon (
            Program program, ArtifactRepositoryLayout artifactRepositoryLayout )
    {
        org.codehaus.mojo.appassembler.model.Daemon daemon = new org.codehaus.mojo.appassembler.model.Daemon ( );

        daemon.setId ( program.getName ( ) );
        daemon.setMainClass ( program.getMainClass ( ) );
        daemon.setShowConsoleWindow ( showConsoleWindow );
        daemon.setCommandLineArguments ( program.getCommandLineArguments ( ) );

        if ( program.getLicenseHeaderFile ( ) != null )
        {
            getLog ( ).debug (
                    "Using the program specific license header. :"
                            + program.getLicenseHeaderFile ( ) );
            daemon.setLicenseHeaderFile ( program.getLicenseHeaderFile ( )
                    .getPath ( ) );
        }
        else
        {
            getLog ( ).debug (
                    "Using the global defined license header. :"
                            + licenseHeaderFile );

            if ( licenseHeaderFile != null )
            {
                daemon.setLicenseHeaderFile ( this.licenseHeaderFile
                        .getAbsolutePath ( ) );
            }
            else
            {
                daemon.setLicenseHeaderFile ( null );
            }
        }

        List directories = new ArrayList ( );

        if ( includeConfigurationDirectoryInClasspath )
        {
            Directory directory = new Directory ( );
            directory.setRelativePath ( configurationDirectory );
            directories.add ( directory );
        }

        if ( daemon.getClasspath ( ) == null )
        {
            daemon.setClasspath ( new Classpath ( ) );
        }

        daemon.getClasspath ( ).setDirectories ( directories );

        daemon.setRepositoryName ( repositoryName );

        List dependencies = new ArrayList ( );

        // TODO: This should be done in a more elegant way for 2.0
        // TODO: Check if the classpath wildcard could be used for Daemons as well?
        if ( isUseAsterikClassPath ( ) )
        {
            Dependency dependency = new Dependency ( );
            dependency.setGroupId ( "" );
            dependency.setArtifactId ( "" );
            dependency.setVersion ( "" );
            dependency.setRelativePath ( "*" );
            dependencies.add ( dependency );
        }
        else
        {
            List classPathArtifacts = new ArrayList ( );

            if ( isProjectArtifactFirstInClassPath ( ) )
            {
                classPathArtifacts.add ( projectArtifact );
                classPathArtifacts.addAll ( artifacts );
            }
            else
            {
                classPathArtifacts.addAll ( artifacts );
                classPathArtifacts.add ( projectArtifact );
            }

            for ( Iterator it = classPathArtifacts.iterator ( ); it.hasNext ( ); )
            {
                Artifact artifact = ( Artifact ) it.next ( );
                Dependency dependency = new Dependency ( );
                dependency.setGroupId ( artifact.getGroupId ( ) );
                dependency.setArtifactId ( artifact.getArtifactId ( ) );
                dependency.setVersion ( artifact.getVersion ( ) );
                dependency.setRelativePath ( artifactRepositoryLayout
                        .pathOf ( artifact ) );
                dependencies.add ( dependency );
            }

        }

        daemon.getClasspath ( ).setDependencies ( dependencies );

        daemon.setJvmSettings ( convertToJvmSettingsWithDefaultHandling ( program ) );

        daemon.setEnvironmentSetupFileName ( this.environmentSetupFileName );

        return daemon;
    }

    private JvmSettings convertToJvmSettingsWithDefaultHandling ( Program program )
    {
        JvmSettings jvmSettings = new JvmSettings ( );

        if ( program.getJvmSettings ( ) != null )
        {
            // Some kind of settings done on per program base so they take
            // precendence.
            jvmSettings = program.getJvmSettings ( );
        }
        else
        {
            // No settings in the program done so we use the default behaviour
            if ( StringUtils.isNotBlank ( this.extraJvmArguments ) )
            {
                jvmSettings
                        .setExtraArguments ( parseTokens ( this.extraJvmArguments ) );
            }
        }

        return jvmSettings;
    }

    // ----------------------------------------------------------------------
    // Install artifacts into the assemble repository
    // ----------------------------------------------------------------------

    private void installArtifact ( ArtifactRepository artifactRepository,
            Artifact artifact ) throws MojoExecutionException
    {
        try
        {
            // Necessary for the artifact's baseVersion to be set correctly
            // See:
            // http://mail-archives.apache.org/mod_mbox/maven-dev/200511.mbox/%3c437288F4.4080003@apache.org%3e
            artifact.isSnapshot ( );

            if ( artifact.getFile ( ) != null )
            {
                getLog ( ).debug (
                        "installArtifact: scope:" + artifact.getScope ( )
                                + " id:" + artifact.getId ( ) );
                artifactInstaller.install ( artifact.getFile ( ), artifact,
                        artifactRepository );
            }
        }
        catch ( ArtifactInstallationException e )
        {
            throw new MojoExecutionException ( "Failed to copy artifact.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Set up the assemble environment
    // ----------------------------------------------------------------------

    private void setUpWorkingArea () throws MojoFailureException
    {
        // create (if necessary) directory for bin files
        File binDir = new File ( assembleDirectory.getAbsolutePath ( ),
                binFolder.toString ( ) );

        if ( !binDir.exists ( ) )
        {

            boolean success = binDir.mkdirs ( );

            if ( !success )
            {
                throw new MojoFailureException (
                        "Failed to create directory for bin files." );
            }
        }
    }

    private void copyConfigurationDirectory () throws MojoFailureException
    {
        if ( !configurationSourceDirectory.exists ( ) )
        {
            throw new MojoFailureException (
                    "The source directory for configuration files does not exist: "
                            + configurationSourceDirectory.getAbsolutePath ( ) );
        }

        File configurationTargetDirectory = new File (
                assembleDirectory.getAbsolutePath ( ), configurationDirectory );
        if ( !configurationTargetDirectory.exists ( ) )
        {
            // Create (if necessary) target directory for configuration files
            boolean success = configurationTargetDirectory.mkdirs ( );

            if ( !success )
            {
                throw new MojoFailureException (
                        "Failed to create the target directory for configuration files: "
                                + configurationTargetDirectory
                                        .getAbsolutePath ( ) );
            }

            try
            {
                getLog ( ).debug (
                        "Will try to copy configuration files from "
                                + configurationSourceDirectory
                                        .getAbsolutePath ( )
                                + " to "
                                + configurationTargetDirectory
                                        .getAbsolutePath ( ) );
                FileUtils.copyDirectory ( configurationSourceDirectory,
                        configurationTargetDirectory, null,
                        getDefaultExcludesAsCommaSeparatedString ( ) );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException (
                        "Failed to copy the configuration files." );
            }
        }
    }

    private String getDefaultExcludesAsCommaSeparatedString ()
    {
        StringBuffer defaultExcludes = new StringBuffer ( );

        List defaultExcludesAsList = FileUtils.getDefaultExcludesAsList ( );
        Iterator iterator = defaultExcludesAsList.iterator ( );
        while ( iterator.hasNext ( ) )
        {
            String exclude = ( String ) iterator.next ( );
            defaultExcludes.append ( exclude );
            if ( iterator.hasNext ( ) )
            {
                defaultExcludes.append ( "," );
            }
        }

        return defaultExcludes.toString ( );
    }

    private Set validatePlatforms ( Set platformsToValidate, Set defaultPlatforms )
            throws MojoFailureException
    {
        if ( platformsToValidate == null )
        {
            return defaultPlatforms;
        }

        if ( platformsToValidate.size ( ) == 1
                && platformsToValidate.iterator ( ).next ( ).equals ( "all" ) )
        {
            return VALID_PLATFORMS;
        }

        if ( !VALID_PLATFORMS.containsAll ( platformsToValidate ) )
        {
            throw new MojoFailureException (
                    "Non-valid default platform declared, supported types are: "
                            + VALID_PLATFORMS );
        }

        return platformsToValidate;
    }

    /**
     * This will tokenize the given argument or give the extraJvmArguments back
     * if the given argument is empty.
     * 
     * @param arg
     *            The argument to parse.
     * @return List of arguments.
     */
    public static List parseTokens ( String arg )
    {
        List extraJvmArguments = new ArrayList ( );

        if ( StringUtils.isEmpty ( arg ) )
        {
            return extraJvmArguments;
        }

        StringTokenizer tokenizer = new StringTokenizer ( arg );

        String argument = null;

        while ( tokenizer.hasMoreTokens ( ) )
        {
            String token = tokenizer.nextToken ( );

            if ( argument != null )
            {
                if ( token.length ( ) == 0 )
                {
                    // ignore it
                    continue;
                }

                int length = token.length ( );

                if ( token.charAt ( length - 1 ) == '\"' )
                {
                    extraJvmArguments.add ( argument + " "
                            + token.substring ( 0, length - 1 ) );
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
                if ( token.charAt ( 0 ) == '\"' )
                {
                    argument = token.substring ( 1 );
                }
                else
                {
                    extraJvmArguments.add ( token );
                }
            }
        }

        return extraJvmArguments;
    }

    public void setAvailableRepositoryLayouts ( Map availableRepositoryLayouts )
    {
        this.availableRepositoryLayouts = availableRepositoryLayouts;
    }

    /**
     * Set the extensions for bin files for the supported platforms. The values
     * are taken from the Mojo's <code>binFileExtensions</code> parameter.
     */
    private void setBinFileExtensions () throws MojoFailureException
    {
        if ( binFileExtensions != null )
        {
            Set keySet = binFileExtensions.keySet ( );
            Iterator iterator = keySet.iterator ( );
            while ( iterator.hasNext ( ) )
            {
                String platformName = ( String ) iterator.next ( );
                if ( !VALID_PLATFORMS.contains ( platformName ) )
                {
                    getLog ( ).warn (
                            "Bin file extension configured for a non-valid platform ("
                                    + platformName
                                    + "), supported platforms are: "
                                    + VALID_PLATFORMS );
                }
                else
                {
                    try
                    {
                        Platform platform = Platform.getInstance ( platformName );
                        platform.setBinFileExtension ( ( String ) binFileExtensions
                                .get ( platformName ) );
                    }
                    catch ( DaemonGeneratorException e )
                    {
                        getLog ( ).warn (
                                "Unable to set the bin file extension for "
                                        + platformName, e );
                    }
                }
            }
        }
    }

    public boolean isProjectArtifactFirstInClassPath ()
    {
        return projectArtifactFirstInClassPath;
    }

    public void setProjectArtifactFirstInClassPath (
            boolean projectArtifactFirstInClassPath )
    {
        this.projectArtifactFirstInClassPath = projectArtifactFirstInClassPath;
    }

    public boolean isUseAllDependencies ()
    {
        return useAllDependencies;
    }

    public void setUseAllDependencies ( boolean useAllDependencies )
    {
        this.useAllDependencies = useAllDependencies;
    }

    public boolean isUseAsterikClassPath ()
    {
        return useAsterikClassPath;
    }

    public void setUseAsterikClassPath ( boolean useAsterikClassPath )
    {
        this.useAsterikClassPath = useAsterikClassPath;
    }
}
