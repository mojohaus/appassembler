package org.codehaus.mojo.appassembler;

/*
 * The MIT License
 *
 * Copyright (c) 2006-2013, The Codehaus
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
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is intended to collect all generic parts of the script generating Mojos assemble and generate-daemons into a
 * single class. A first step of hopefully merging the two into one some day.
 *
 * @author Dennis Lundberg
 * @version $Id$
 */
public abstract class AbstractScriptGeneratorMojo
    extends AbstractAppAssemblerMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * Setup file in <code>$BASEDIR/bin</code> to be called prior to execution.
     * <p>
     * <b>Note:</b> only for the <code>jsw</code> platform. If this optional environment file also sets up
     * WRAPPER_CONF_OVERRIDES variable, it will be passed into JSW native launcher's command line arguments to override
     * wrapper.conf's properties. See http://wrapper.tanukisoftware.com/doc/english/props-command-line.html for details.
     * </p>
     *
     * @since 1.2.3 (generate-daemons)
     */
    @Parameter
    protected String environmentSetupFileName;

    /**
     * You can define a license header file which will be used instead the default header in the generated scripts.
     *
     * @since 1.2
     */
    @Parameter
    protected File licenseHeaderFile;

    /**
     * The unix template of the generated script. It can be a file or resource path. If not given, an internal one is
     * used. Use with care since it is not guaranteed to be compatible with new plugin release.
     *
     * @since 1.3
     */
    @Parameter( property = "unixScriptTemplate" )
    protected String unixScriptTemplate;

    /**
     * Sometimes it happens that you have many dependencies which means in other words having a very long classpath. And
     * sometimes the classpath becomes too long (in particular on Windows based platforms). This option can help in such
     * situation. If you activate that your classpath contains only a <a href=
     * "http://docs.oracle.com/javase/6/docs/technotes/tools/windows/classpath.html" >classpath wildcard</a> (REPO/*).
     * But be aware that this works only in combination with Java 1.6 and above and with {@link #repositoryLayout}
     * <code>flat</code>. Otherwise this configuration will not work.
     *
     * @since 1.2.3 (assemble), 1.3.1 (generate-daemons)
     */
    @Parameter( defaultValue = "false" )
    protected boolean useWildcardClassPath;

    /**
     * The windows template of the generated script. It can be a file or resource path. If not given, an internal one is
     * used. Use with care since it is not guaranteed to be compatible with new plugin release.
     *
     * @since 1.3
     */
    @Parameter( property = "windowsScriptTemplate" )
    protected String windowsScriptTemplate;

    // -----------------------------------------------------------------------
    // Read-only Parameters
    // -----------------------------------------------------------------------

    @Parameter( defaultValue = "${project.runtimeArtifacts}", readonly = true )
    protected List<Artifact> artifacts;

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    protected MavenProject mavenProject;

    /**
     * Set to <code>false</code> to skip repository generation.
     */
    @Parameter( defaultValue = "true" )
    protected boolean generateRepository;

    /**
     * The name of the target directory for configuration files. Prior to version 1.7 this value defaults to 'conf' for assemble goal and 'etc' for generate-daemons
     */
    @Parameter( defaultValue = "etc" )
    protected String configurationDirectory;

    /**
     * The name of the source directory for configuration files.
     *
     * @since 1.1
     */
    @Parameter( defaultValue = "src/main/config" )
    protected File configurationSourceDirectory;

    /**
     * If the source configuration directory should be copied to the configured <code>configurationDirectory</code>.
     *
     * @since 1.1 (assemble), 1.7 (generate-daemons)
     */
    @Parameter( defaultValue = "false" )
    protected boolean copyConfigurationDirectory;

    /**
     * Location under base directory where all of files non-recursively are added before the generated classpath. Java
     * 6+ only since it uses wildcard classpath format. This is a convenient way to have user to add artifacts that not
     * possible to be part of final assembly such as LGPL/GPL artifacts
     *
     * @since 1.6
     */
    @Parameter
    protected String endorsedDir;

    /**
     * Project build filters.
     *
     * @since 1.8
     */
    @Parameter( defaultValue = "${project.build.filters}", readonly = true )
    protected List<String> buildFilters;

    /**
     * The character encoding scheme to be applied when filtering the source
     * configuration directory.
     * 
     * @since 1.8
     */
    @Parameter( defaultValue = "${project.build.sourceEncoding}" )
    protected String encoding;

    /**
     * Expressions preceded with this String won't be interpolated.
     * <code>\${foo}</code> will be replaced with <code>${foo}</code>.
     *
     * @since 1.8
     */
    @Parameter
    protected String escapeString;

    /**
     * If the source configuration directory should be filtered when copied to
     * the configured <code>configurationDirectory</code>.
     *
     * @since 1.8
     */
    @Parameter( defaultValue = "false" )
    protected boolean filterConfigurationDirectory;

    /**
     * @since 1.8
     */
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    /**
     * The logs directory for the assembled project.
     * If you specify a value for this parameter an empty directory with the name given will be created.
     *
     * @since 1.8
     */
    @Parameter( property = "logsDirectory" )
    protected String logsDirectory;

    /**
     * The temp directory of the assembled project.
     * If you specify a value for this parameter an empty directory with the name given will be created.
     *
     * @since 1.8
     */
    @Parameter( property = "tempDirectory" )
    protected String tempDirectory;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    @Component
    protected DaemonGeneratorService daemonGeneratorService;

    /**
     * The filtering component used when copying the source configuration
     * directory.
     * 
     * @since 1.8
     */
    @Component( role = MavenResourcesFiltering.class, hint = "default" )
    protected MavenResourcesFiltering mavenResourcesFiltering;

    protected void doCopyConfigurationDirectory( final String targetDirectory )
        throws MojoFailureException
    {
        if ( !configurationSourceDirectory.exists() )
        {
            throw new MojoFailureException( "The source directory for configuration files does not exist: "
                + configurationSourceDirectory.getAbsolutePath() );
        }

        getLog().debug( "copying configuration directory." );

        // Create a Resource from the configuration source directory
        Resource resource = new Resource();
        resource.setDirectory( configurationSourceDirectory.getAbsolutePath() );
        resource.setFiltering( filterConfigurationDirectory );
        resource.setTargetPath( configurationDirectory );
        List<Resource> resources = new ArrayList<Resource>();
        resources.add( resource );

        MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution( resources,
                                                                                       new File( targetDirectory ),
                                                                                       mavenProject, encoding,
                                                                                       buildFilters,
                                                                                       Collections.<String>emptyList(),
                                                                                       session );

        mavenResourcesExecution.setEscapeString( escapeString );
        // Include empty directories, to be backwards compatible
        mavenResourcesExecution.setIncludeEmptyDirs( true );
        mavenResourcesExecution.setUseDefaultFilterWrappers( true );

        // @todo Possible future enhancements
        // mavenResourcesExecution.setEscapedBackslashesInFilePath( escapedBackslashesInFilePath );
        // mavenResourcesExecution.setOverwrite( overwrite );
        // mavenResourcesExecution.setSupportMultiLineFiltering( supportMultiLineFiltering );

        try
        {
            getLog().debug( "Will try to copy configuration files from "
                                + configurationSourceDirectory.getAbsolutePath() + " to "
                                + targetDirectory + FileUtils.FS + configurationDirectory );

            // Use a MavenResourcesFiltering component to filter and copy the configuration files
            mavenResourcesFiltering.filterResources( mavenResourcesExecution );
        }
        catch ( MavenFilteringException mfe )
        {
            throw new MojoFailureException( "Failed to copy/filter the configuration files." );
        }
    }

    // -----------------------------------------------------------------------
    // Protected helper methods.
    // -----------------------------------------------------------------------

    protected void doCreateExtraDirectories( File targetDirectory )
        throws MojoFailureException
    {
        if ( this.logsDirectory != null )
        {
            File logsDirectory = new File( targetDirectory, this.logsDirectory );
            boolean success = logsDirectory.mkdirs();
            if ( !success )
            {
                throw new MojoFailureException( "Failed to create directory for log files." );
            }
        }
        if ( this.tempDirectory != null )
        {
            File tempDirectory = new File( targetDirectory, this.tempDirectory );
            boolean success = tempDirectory.mkdirs();
            if ( !success )
            {
                throw new MojoFailureException( "Failed to create directory for temp files." );
            }
        }
    }

    protected void installDependencies( final String outputDirectory, final String repositoryName )
        throws MojoExecutionException, MojoFailureException
    {
        if ( generateRepository )
        {
            // The repo where the jar files will be installed
            ArtifactRepository artifactRepository =
                artifactRepositoryFactory.createDeploymentArtifactRepository( "appassembler", "file://"
                    + outputDirectory + "/" + repositoryName, getArtifactRepositoryLayout(), false );

            for ( Artifact artifact : artifacts )
            {
                installArtifact( artifact, artifactRepository, this.useTimestampInSnapshotFileName );
            }

            // install the project's artifact in the new repository
            installArtifact( projectArtifact, artifactRepository );
        }
    }
}
