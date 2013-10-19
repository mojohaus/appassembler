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

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService;

import java.io.File;
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
    private boolean useWildcardClassPath;

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

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /**
     * @component
     */
    protected DaemonGeneratorService daemonGeneratorService;

    // -----------------------------------------------------------------------
    // Getters and setters
    // -----------------------------------------------------------------------

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
