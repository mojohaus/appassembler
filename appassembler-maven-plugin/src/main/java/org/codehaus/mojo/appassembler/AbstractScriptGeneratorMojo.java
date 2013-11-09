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

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService;
import org.codehaus.mojo.appassembler.util.FileFilterHelper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
     * @parameter
     * @since 1.2.3 (generate-daemons)
     */
    protected String environmentSetupFileName;

    /**
     * You can define a license header file which will be used instead the default header in the generated scripts.
     * 
     * @parameter
     * @since 1.2
     */
    protected File licenseHeaderFile;

    /**
     * The unix template of the generated script. It can be a file or resource path. If not given, an internal one is
     * used. Use with case since it is not guaranteed to be compatible with new plugin release.
     * 
     * @parameter expression="${unixScriptTemplate}"
     * @since 1.3
     */
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
     * @parameter default-value="false"
     */
    protected boolean useWildcardClassPath;

    /**
     * The windows template of the generated script. It can be a file or resource path. If not given, an internal one is
     * used. Use with care since it is not guaranteed to be compatible with new plugin release.
     * 
     * @parameter expression="${windowsScriptTemplate}"
     * @since 1.3
     */
    protected String windowsScriptTemplate;

    // -----------------------------------------------------------------------
    // Read-only Parameters
    // -----------------------------------------------------------------------

    /**
     * @readonly
     * @parameter expression="${project.runtimeArtifacts}"
     */
    protected List artifacts;

    /**
     * @readonly
     * @required
     * @parameter expression="${project}"
     */
    protected MavenProject mavenProject;
    
    /**
     * Set to <code>false</code> to skip repository generation.
     * 
     * @parameter default-value="true"
     */
    protected boolean generateRepository;
    
    /**
     * The name of the target directory for configuration files.
     * 
     * @parameter default-value="etc"
     */
    protected String configurationDirectory;
    
    /**
     * The name of the source directory for configuration files.
     * 
     * @parameter default-value="src/main/config"
     * @since 1.1
     */
    protected File configurationSourceDirectory;
    
    /**
     * If the source configuration directory should be copied to the configured <code>configurationDirectory</code>.
     * 
     * @parameter default-value="false"
     * @since 1.1
     */
    protected boolean copyConfigurationDirectory;
    
    /**
     * Location under base directory where all of files non-recursively are added before the generated classpath. 
     * Java 6+ only since it uses wildcard classpath format.
     * This is a convenient way to have user to add artifacts that not possible to be part of final assembly
     * such as LGPL/GPL artifacts
     * 
     * @parameter
     * @since 1.6
     */
    protected String endorsedDir;
    
    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /**
     * @component
     */
    protected DaemonGeneratorService daemonGeneratorService;

    protected void doCopyConfigurationDirectory(final String targetDirectory) throws MojoFailureException {
        if (!configurationSourceDirectory.exists()) {
            throw new MojoFailureException("The source directory for configuration files does not exist: "
                + configurationSourceDirectory.getAbsolutePath());
        }

        getLog().debug("copying configuration directory.");
        
        File configurationTargetDirectory = new File(targetDirectory, configurationDirectory);

        if (!configurationTargetDirectory.exists()) {
            // Create (if necessary) target directory for configuration files
            boolean success = configurationTargetDirectory.mkdirs();

            if (!success) {
                throw new MojoFailureException("Failed to create the target directory for configuration files: "
                    + configurationTargetDirectory.getAbsolutePath());
            }
        }
        try {
            getLog().debug("Will try to copy configuration files from "
                               + configurationSourceDirectory.getAbsolutePath() + " to "
                               + configurationTargetDirectory.getAbsolutePath());

            FileUtils.copyDirectory(configurationSourceDirectory, configurationTargetDirectory,
                                    FileFilterHelper.createDefaultFilter());
        } catch (IOException e) {
            throw new MojoFailureException("Failed to copy the configuration files.");
        }
    }
    
    // -----------------------------------------------------------------------
    // Protected helper methods.
    // -----------------------------------------------------------------------
    
    protected void installDependencies(final String outputDirectory, final String repositoryName)
        throws MojoExecutionException, MojoFailureException {
        if ( generateRepository ) {
            // The repo where the jar files will be installed
            ArtifactRepository artifactRepository =
                artifactRepositoryFactory.createDeploymentArtifactRepository("appassembler", "file://"
                    + outputDirectory + "/" + repositoryName, getArtifactRepositoryLayout(), false);

            for (Iterator it = artifacts.iterator(); it.hasNext();) {
                Artifact artifact = (Artifact) it.next();
                installArtifact(artifact, artifactRepository, this.useTimestampInSnapshotFileName);
            }

            // install the project's artifact in the new repository
            installArtifact(projectArtifact, artifactRepository);
        }
    }
    
}
