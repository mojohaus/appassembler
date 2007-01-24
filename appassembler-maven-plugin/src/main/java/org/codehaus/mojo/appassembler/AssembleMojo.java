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
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.repository.layout.LegacyRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Assembles the artifacts and generates bin scripts for the configured applications
 *
 * @goal assemble
 * @requiresDependencyResolution runtime
 * @phase package
 *
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 */
public class AssembleMojo
    extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------------

    /**
     * @readonly
     * @parameter expression="${project.build.directory}"
     */
    private String buildDirectory;

    /**
     * @readonly
     * @parameter expression="${project.artifacts}"
     */
    private Set artifacts;

    /**
     * The directory that will be used to assemble the artifacts in
     * and place the bin scripts.
     *
     * @required
     * @parameter expression="${project.build.directory}/appassembler"
     */
    private File assembleDirectory;

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
     * The set of Programs that bin files will be generated for.
     *
     * @required
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
     * Include /etc in the beginning of the classpath in the generated bin files
     *
     * @parameter default-value="true"
     */
    private boolean includeConfigurationDirectoryInClasspath;

    /**
     * The layout of the generated Maven repository.
     * Supported types - "default" (Maven2) | "legacy" (Maven1)
     *
     * @parameter default="default'
     */
    private String repositoryLayout;

    /**
     * Extra arguments that will be given to the JVM verbatim.
     *
     * @parameter
     */
    private String extraJvmArguments;

    /**
     * The default platforms the plugin will generate bin files for.
     * Configure with string values - "all"(default/empty) | "windows" | "unix"
     *
     * @parameter
     */
    private Set platforms;

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

    /**
     * The repo where the jar files will be installed
     */
    private ArtifactRepository artifactRepository;

    /**
     * The layout of the repository.
     */
    private ArtifactRepositoryLayout artifactRepositoryLayout;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private PlatformUtil windowsPlatformUtil = new PlatformUtil( true );

    private PlatformUtil unixPlatformUtil = new PlatformUtil( false );

    private boolean defaultPlatformWindows = true;

    private boolean defaultPlatformUnix = true;

    // ----------------------------------------------------------------------
    // Validate
    // ----------------------------------------------------------------------

    public void validate()
        throws MojoFailureException, MojoExecutionException
    {
        // ----------------------------------------------------------------------
        // Create new repository for dependencies
        // ----------------------------------------------------------------------

        if ( repositoryLayout == null || repositoryLayout.equals( "default" ) )
        {
            artifactRepositoryLayout = new DefaultRepositoryLayout();
        }
        else if ( repositoryLayout.equals( "legacy" ) )
        {
            artifactRepositoryLayout = new LegacyRepositoryLayout();
        }
        else
        {
            throw new MojoFailureException( "Unknown repository layout '" + repositoryLayout + "'." );
        }

        // ----------------------------------------------------------------------
        // Validate default platform configuration
        // ----------------------------------------------------------------------

        Set validPlatforms = new HashSet();
        validPlatforms.add( "all" );
        validPlatforms.add( "windows" );
        validPlatforms.add( "unix" );

        if ( platforms != null )
        {
            if ( !validPlatforms.containsAll( platforms ) )
            {
                throw new MojoFailureException(
                    "Non-valid default platform declared, supported types are: 'all', 'windows' and 'unix'" );
            }

            defaultPlatformWindows = false;
            defaultPlatformUnix = false;

            if ( platforms.contains( "all" ) )
            {
                defaultPlatformWindows = true;
                defaultPlatformUnix = true;
            }

            if ( platforms.contains( "windows" ) )
            {
                defaultPlatformWindows = true;
            }
            if ( platforms.contains( "unix" ) )
            {
                defaultPlatformUnix = true;
            }
        }

        // ----------------------------------------------------------------------
        // Validate Programs
        // ----------------------------------------------------------------------

        for ( Iterator i = programs.iterator(); i.hasNext(); )
        {
            Program program = (Program) i.next();

            if ( program.getMainClass() == null || program.getMainClass().trim().equals( "" ) )
            {
                throw new MojoFailureException( "Missing main class in Program configuration" );
            }

            // platforms
            if ( program.getPlatforms() != null )
            {
                if ( !validPlatforms.containsAll( program.getPlatforms() ) )
                {
                    throw new MojoFailureException(
                        "Non-valid platform for program declared, supported types are: 'all', 'windows' and 'unix'" );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Execute
    // ----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // validate input and set defaults
        validate();

        artifactRepository = artifactRepositoryFactory.createDeploymentArtifactRepository( "appassembler", "file://" +
            assembleDirectory.getAbsolutePath() + "/repo", artifactRepositoryLayout, false );

        // ----------------------------------------------------------------------
        // Install dependencies in the new repository
        // ----------------------------------------------------------------------

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            installArtifact( artifact );
        }

        // install the project's artifact in the new repository
        installArtifact( projectArtifact );

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

            if ( program.getPlatforms() == null )
            {
                if ( defaultPlatformWindows )
                {
                    createBinScript( program, windowsPlatformUtil );
                }
                if ( defaultPlatformUnix )
                {
                    createBinScript( program, unixPlatformUtil );
                }
            }
            else
            {
                if ( program.getPlatforms().contains( "all" ) )
                {
                    createBinScript( program, windowsPlatformUtil );
                    createBinScript( program, unixPlatformUtil );
                    break;
                }

                if ( program.getPlatforms().contains( "windows" ) )
                {
                    createBinScript( program, windowsPlatformUtil );
                }

                if ( program.getPlatforms().contains( "unix" ) )
                {
                    createBinScript( program, unixPlatformUtil );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Install artifacts into the assemble repository
    // ----------------------------------------------------------------------

    private void installArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        try
        {
            // Necessary for the artifact's baseVersion to be set correctly
            // See: http://mail-archives.apache.org/mod_mbox/maven-dev/200511.mbox/%3c437288F4.4080003@apache.org%3e
            artifact.isSnapshot();

            artifactInstaller.install( artifact.getFile(), artifact, artifactRepository );
        }
        catch ( ArtifactInstallationException e )
        {
            throw new MojoExecutionException( "Failed to copy artifact.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Create bin file
    // ----------------------------------------------------------------------

    private void createBinScript( Program program, PlatformUtil platformUtil )
        throws MojoExecutionException
    {
        try
        {
            InputStream in = this.getClass().getResourceAsStream( platformUtil.getBinTemplate() );

            InputStreamReader reader = new InputStreamReader( in );

            Map context = new HashMap();
            context.put( "MAINCLASS", program.getMainClass() );
            context.put( "CLASSPATH", platformUtil.getClassPath() );
            context.put( "EXTRA_JVM_ARGUMENTS", platformUtil.getExtraJvmArguments() );
            context.put( "APP_NAME", program.getName() );

            InterpolationFilterReader interpolationFilterReader =
                new InterpolationFilterReader( reader,
                                               context,
                                               platformUtil.getInterpolationToken(),
                                               platformUtil.getInterpolationToken() );

            // Set the name of the bin file
            String programName = "";

            if ( program.getName() == null || program.getName().trim().equals( "" ) )
            {
                // Get class name and use it as the filename
                StringTokenizer tokenizer = new StringTokenizer( program.getMainClass(), "." );
                while ( tokenizer.hasMoreElements() )
                {
                    programName = tokenizer.nextToken();
                }

                programName = programName.toLowerCase();
            }
            else
            {
                programName = program.getName();
            }

            // Set bin prefix
            if ( binPrefix != null )
            {
                programName = binPrefix.trim() + programName;
            }

            String binFileName = programName + platformUtil.getBinFileExtension();

            File binFile = new File( assembleDirectory.getAbsolutePath() + "/bin", binFileName );
            FileWriter out = new FileWriter( binFile );

            try
            {
                IOUtil.copy( interpolationFilterReader, out );
            }
            finally
            {
                IOUtil.close( interpolationFilterReader );
                IOUtil.close( out );
            }
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
            boolean success = new File( assembleDirectory.getAbsolutePath(), "bin" ).mkdir();

            if ( !success )
            {
                throw new MojoFailureException( "Failed to create directory for bin files." );
            }
        }
    }

    // ----------------------------------------------------------------------
    // PlatformUtil
    // ----------------------------------------------------------------------

    private class PlatformUtil
    {
        boolean isWindows;

        public PlatformUtil( boolean isWindows )
        {
            this.isWindows = isWindows;
        }

        public String getClassPath()
        {
            String classPath = "";

            // include the project's own artifact in the classpath
            Set classPathArtifacts = new HashSet( artifacts );
            classPathArtifacts.add( projectArtifact );

            if ( includeConfigurationDirectoryInClasspath )
            {
                if ( isWindows )
                {
                    classPath += "\"%BASEDIR%\"\\etc;";
                }
                else
                {
                    classPath += "\"$BASEDIR\"/etc:";
                }
            }

            for ( Iterator it = classPathArtifacts.iterator(); it.hasNext(); )
            {
                Artifact artifact = (Artifact) it.next();

                if ( isWindows )
                {
                    String path = artifactRepositoryLayout.pathOf( artifact );

                    path = path.replace( '/', '\\' );
                    classPath += "%REPO%\\" + path + ";";
                }
                else
                {
                    classPath += "$REPO/" + artifactRepositoryLayout.pathOf( artifact ) + ":";
                }
            }

            return classPath;
        }

        public String getBinTemplate()
        {
            if ( isWindows )
            {
                return "/windowsBinTemplate";
            }
            else
            {
                return "/unixBinTemplate";
            }
        }

        public String getInterpolationToken()
        {
            if ( isWindows )
            {
                return "#";
            }
            else
            {
                return "@";
            }
        }

        public String getBinFileExtension()
        {
            if ( isWindows )
            {
                return ".bat";
            }
            else
            {
                return "";
            }
        }

        public String getExtraJvmArguments()
        {
            extraJvmArguments = StringUtils.clean( extraJvmArguments );

            String repo;

            if ( isWindows )
            {
                repo = "%REPO%";
            }
            else
            {
                repo = "\"\\$REPO\"";
            }

            return extraJvmArguments.replaceAll( "#REPO#", repo );
        }
    }
}
