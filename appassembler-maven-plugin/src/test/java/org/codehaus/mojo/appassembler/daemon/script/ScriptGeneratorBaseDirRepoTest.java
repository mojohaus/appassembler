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
package org.codehaus.mojo.appassembler.daemon.script;

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author <a href="mailto:khmarbaise@soebes.de">Karl Heinz Marbaise</a>
 * @since 1.2
 */
public class ScriptGeneratorBaseDirRepoTest
        extends PlexusTestCase
{
    private static final String PREFIX = "src/test/resources/org/codehaus/mojo/appassembler/daemon/script-basedir-repo/";

    protected void setUp()
            throws Exception
    {
        super.setUp();

        LoggerManager loggerManager = (LoggerManager) lookup(LoggerManager.ROLE);

        loggerManager.setThreshold(Logger.LEVEL_DEBUG);
    }

    public void testNormalShellScriptGenerationWithBaseDir()
            throws Exception
    {
        for (Iterator it = Platform.getAllPlatforms().iterator(); it.hasNext();)
        {
            testNormalShellScriptGenerationWithBaseDir((Platform) it.next());
        }
    }

    private void testNormalShellScriptGenerationWithBaseDir(Platform platform)
            throws Exception
    {
        ScriptGenerator generator = (ScriptGenerator) lookup(ScriptGenerator.ROLE);

        Daemon daemon = new Daemon();

        daemon.setId("basedir-test");
        daemon.setMainClass("foo.Bar");
        daemon.setJvmSettings(new JvmSettings());
        daemon.getJvmSettings().setExtraArguments(Arrays.asList(new String[] { "Yo", "dude", "xyz=@BASEDIR@" }));
        daemon.setEnvironmentSetupFileName("setup");
        daemon.setRepositoryName("repo");
        File outputDirectory = getTestFile("target/test-output/normal-shell/" + platform.getName());

        generator.createBinScript(platform.getName(), daemon, outputDirectory, "bin");

        File expectedFile = getTestFile(PREFIX + "expected-" + daemon.getId() + platform.getBinFileExtension());
        File actualFile = new File(outputDirectory, "bin/" + daemon.getId() + platform.getBinFileExtension());

        assertEquals(FileUtils.fileRead(expectedFile), FileUtils.fileRead(actualFile));
    }


    public void testNormalShellScriptGenerationWithRepo()
            throws Exception
    {
        for (Iterator it = Platform.getAllPlatforms().iterator(); it.hasNext();)
        {
            testNormalShellScriptGenerationWithRepo((Platform) it.next());
        }
    }


    private void testNormalShellScriptGenerationWithRepo(Platform platform)
            throws Exception
    {
        ScriptGenerator generator = (ScriptGenerator) lookup(ScriptGenerator.ROLE);

        Daemon daemon = new Daemon();

        daemon.setId("repo-test");
        daemon.setMainClass("foo.Bar");
        daemon.setJvmSettings(new JvmSettings());
        daemon.getJvmSettings().setExtraArguments(Arrays.asList(new String[] { "Yo", "dude", "xyz=@REPO@" }));
        daemon.setEnvironmentSetupFileName("setup");
        daemon.setRepositoryName("repo");
        File outputDirectory = getTestFile("target/test-output/normal-shell/" + platform.getName());

        generator.createBinScript(platform.getName(), daemon, outputDirectory, "bin" );

        File expectedFile = getTestFile(PREFIX + "expected-" + daemon.getId() + platform.getBinFileExtension());
        File actualFile = new File(outputDirectory, "bin/" + daemon.getId() + platform.getBinFileExtension());

        assertEquals(FileUtils.fileRead(expectedFile), FileUtils.fileRead(actualFile));
    }


}
