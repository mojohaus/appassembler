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
package org.codehaus.mojo.appassembler.daemon.jsw;

import java.io.File;
import java.io.IOException;

import org.codehaus.mojo.appassembler.daemon.AbstractDaemonGeneratorTest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo test could be improved - there are other conditions like
 *       "check if extra properties can override those from template"
 * @todo reading POM/model should not be necessary?
 */
public class JavaServiceWrapperDaemonGeneratorTest
    extends AbstractDaemonGeneratorTest
{
    public void testGenerationWithAllInfoInDescriptor()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-1/pom.xml", "src/test/resources/project-1/descriptor.xml",
                 "target/output-1-jsw" );

        File jswDir = getTestFile( "target/output-1-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        assertEquals( normalizedLineEndingRead( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/wrapper-1.conf" ),
                      FileUtils.fileRead( wrapper ) );

        File shellScript = new File( jswDir, "bin/app" );

        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );

        assertEquals( FileUtils.fileRead( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-1.sh" ) ),
                      FileUtils.fileRead( shellScript ) );

        File batchFile = new File( jswDir, "bin/app.bat" );

        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );

        assertEquals( FileUtils.fileRead( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-1.bat" ) ),
                      FileUtils.fileRead( batchFile ) );

        assertEquals( ( new File( getBasedir()
                          + "/target/classes/org/codehaus/mojo/appassembler/daemon/jsw/bin/wrapper-linux-x86-32" ) ).length(),
                      ( new File( jswDir, "bin/wrapper-linux-x86-32" ) ).length() );

        assertFileExists( jswDir, "lib/wrapper.jar" );
        assertFileExists( jswDir, "lib/wrapper-windows-x86-32.dll" );
        assertFileExists( jswDir, "lib/libwrapper-macosx-universal-32.jnilib" );
        assertFileExists( jswDir, "lib/libwrapper-linux-x86-32.so" );
        assertFileExists( jswDir, "lib/libwrapper-solaris-x86-32.so" );

        assertFileExists( jswDir, "bin/wrapper-windows-x86-32.exe" );
        assertFileExists( jswDir, "bin/wrapper-macosx-universal-32" );
        assertFileExists( jswDir, "bin/wrapper-solaris-x86-32" );
        assertFileExists( jswDir, "bin/wrapper-linux-x86-32" );

        assertFileNotExists( jswDir, "lib/libwrapper-macosx-ppc-32.jnilib" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-ppc-64.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-x86-64.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-solaris-sparc-32.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-solaris-sparc-64.so" );
        assertFileNotExists( jswDir, "bin/wrapper-macosx-ppc-32" );
        assertFileNotExists( jswDir, "bin/wrapper-linux-ppc-64" );
        assertFileNotExists( jswDir, "bin/wrapper-linux-x86-64" );
        assertFileNotExists( jswDir, "bin/wrapper-solaris-sparc-32" );
        assertFileNotExists( jswDir, "bin/wrapper-solaris-sparc-64" );
    }

    public void testGenerateWithCustomJSWPlatforms()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-4/pom.xml", "src/test/resources/project-4/descriptor.xml",
                 "target/output-4-jsw" );

        File jswDir = getTestFile( "target/output-4-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        assertEquals( normalizedLineEndingRead( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/wrapper-1.conf" ),
                      FileUtils.fileRead( wrapper ) );

        File shellScript = new File( jswDir, "bin/app" );

        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );

        assertEquals( FileUtils.fileRead( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-1.sh" ) ),
                      FileUtils.fileRead( shellScript ) );

        File batchFile = new File( jswDir, "bin/app.bat" );

        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );

        assertEquals( FileUtils.fileRead( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-1.bat" ) ),
                      FileUtils.fileRead( batchFile ) );

        assertEquals( ( new File( getBasedir()
                          + "/target/classes/org/codehaus/mojo/appassembler/daemon/jsw/bin/wrapper-linux-x86-32" ) ).length(),
                      ( new File( jswDir, "bin/wrapper-linux-x86-32" ) ).length() );

        assertFileExists( jswDir, "lib/wrapper.jar" );
        assertFileExists( jswDir, "lib/wrapper-windows-x86-32.dll" );
        assertFileExists( jswDir, "lib/libwrapper-macosx-universal-32.jnilib" );
        assertFileNotExists( jswDir, "lib/libwrapper-macosx-ppc-32.jnilib" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-ppc-64.so" );
        assertFileExists( jswDir, "lib/libwrapper-linux-x86-32.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-x86-64.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-solaris-x86-32.so" );
        assertFileExists( jswDir, "lib/libwrapper-solaris-sparc-64.so" );

        assertFileExists( jswDir, "bin/wrapper-windows-x86-32.exe" );
        assertFileExists( jswDir, "bin/wrapper-macosx-universal-32" );
        assertFileNotExists( jswDir, "bin/wrapper-macosx-ppc-32" );
        assertFileNotExists( jswDir, "bin/wrapper-linux-ppc-64" );
        assertFileExists( jswDir, "bin/wrapper-linux-x86-32" );
        assertFileNotExists( jswDir, "bin/wrapper-linux-x86-64" );
        assertFileNotExists( jswDir, "bin/wrapper-solaris-x86-32" );
        assertFileExists( jswDir, "bin/wrapper-solaris-sparc-64" );
    }

    public void testDefaultJSWFilesIfNoGeneratorConfigurationsIsSet()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-5/pom.xml", "src/test/resources/project-5/descriptor.xml",
                 "target/output-5-jsw" );

        File jswDir = getTestFile( "target/output-5-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        assertEquals( normalizedLineEndingRead( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/no-generator-configurations-wrapper.conf" ),
                      FileUtils.fileRead( wrapper ) );

        File shellScript = new File( jswDir, "bin/app" );

        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );

        assertEquals( FileUtils.fileRead( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/no-generator-configurations-run.sh" ) ),
                      FileUtils.fileRead( shellScript ) );

        File batchFile = new File( jswDir, "bin/app.bat" );

        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );

        assertEquals( FileUtils.fileRead( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/no-generator-configurations-run.bat" ) ),
                      FileUtils.fileRead( batchFile ) );

        assertEquals( ( new File( getBasedir()
                          + "/target/classes/org/codehaus/mojo/appassembler/daemon/jsw/bin/wrapper-linux-x86-32" ) ).length(),
                      ( new File( jswDir, "bin/wrapper-linux-x86-32" ) ).length() );

        assertFileExists( jswDir, "lib/wrapper.jar" );
        assertFileExists( jswDir, "lib/wrapper-windows-x86-32.dll" );
        assertFileExists( jswDir, "lib/libwrapper-macosx-universal-32.jnilib" );
        assertFileExists( jswDir, "lib/libwrapper-linux-x86-32.so" );
        assertFileExists( jswDir, "lib/libwrapper-solaris-x86-32.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-macosx-ppc-32.jnilib" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-ppc-64.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-x86-64.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-solaris-sparc-32.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-solaris-sparc-64.so" );

        assertFileExists( jswDir, "bin/wrapper-windows-x86-32.exe" );
        assertFileExists( jswDir, "bin/wrapper-macosx-universal-32" );
        assertFileExists( jswDir, "bin/wrapper-linux-x86-32" );
        assertFileExists( jswDir, "bin/wrapper-solaris-x86-32" );
        assertFileNotExists( jswDir, "bin/wrapper-macosx-ppc-32" );
        assertFileNotExists( jswDir, "bin/wrapper-linux-ppc-64" );
        assertFileNotExists( jswDir, "bin/wrapper-linux-x86-64" );
        assertFileNotExists( jswDir, "bin/wrapper-solaris-sparc-32" );
        assertFileNotExists( jswDir, "bin/wrapper-solaris-sparc-64" );
    }

    public void testGenerateWithConfigurationDirectoriesAddedFromGeneratorConfiguration()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-6/pom.xml", "src/test/resources/project-6/descriptor.xml",
                 "target/output-6-jsw" );

        File jswDir = getTestFile( "target/output-6-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        assertEquals( normalizedLineEndingRead( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/wrapper-6.conf" ),
                      FileUtils.fileRead( wrapper ) );
    }

    private String normalizedLineEndingRead( String file )
        throws IOException
    {
        String expected = FileUtils.fileRead( getTestFile( file ) );
        return StringUtils.unifyLineSeparators( expected );
    }

    public void testGenerateWithAllKnownPlatforms()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-7/pom.xml", "src/test/resources/project-7/descriptor.xml",
                 "target/output-7-jsw" );

        File jswDir = getTestFile( "target/output-7-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        assertEquals( normalizedLineEndingRead( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/wrapper-7.conf" ),
                      FileUtils.fileRead( wrapper ) );

        assertFileExists( jswDir, "lib/wrapper.jar" );
        assertFileExists( jswDir, "lib/libwrapper-aix-ppc-32.a" );
        assertFileExists( jswDir, "lib/libwrapper-aix-ppc-64.a" );
        assertFileExists( jswDir, "lib/libwrapper-hpux-parisc-64.sl" );
        assertFileExists( jswDir, "lib/libwrapper-macosx-universal-32.jnilib" );
        assertFileExists( jswDir, "lib/libwrapper-macosx-universal-64.jnilib" );
        assertFileExists( jswDir, "lib/libwrapper-linux-ppc-64.so" );
        assertFileExists( jswDir, "lib/libwrapper-linux-x86-32.so" );
        assertFileExists( jswDir, "lib/libwrapper-linux-x86-64.so" );
        assertFileExists( jswDir, "lib/libwrapper-solaris-x86-32.so" );
        assertFileExists( jswDir, "lib/libwrapper-solaris-sparc-32.so" );
        assertFileExists( jswDir, "lib/libwrapper-solaris-sparc-64.so" );
        assertFileExists( jswDir, "lib/wrapper-windows-x86-32.dll" );
        assertFileExists( jswDir, "lib/wrapper-windows-x86-64.dll" );

        assertFileExists( jswDir, "bin/wrapper-aix-ppc-32" );
        assertFileExists( jswDir, "bin/wrapper-aix-ppc-64" );
        assertFileExists( jswDir, "bin/wrapper-hpux-parisc-64" );
        assertFileExists( jswDir, "bin/wrapper-macosx-universal-32" );
        assertFileExists( jswDir, "bin/wrapper-macosx-universal-64" );
        assertFileExists( jswDir, "bin/wrapper-linux-ppc-64" );
        assertFileExists( jswDir, "bin/wrapper-linux-x86-32" );
        assertFileExists( jswDir, "bin/wrapper-linux-x86-64" );
        assertFileExists( jswDir, "bin/wrapper-solaris-x86-32" );
        assertFileExists( jswDir, "bin/wrapper-solaris-sparc-32" );
        assertFileExists( jswDir, "bin/wrapper-solaris-sparc-64" );
        assertFileExists( jswDir, "bin/wrapper-windows-x86-32.exe" );
        assertFileExists( jswDir, "bin/wrapper-windows-x86-64.exe" );
    }

    public void testGenerationWithRunAsUserEnvVar()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-8/pom.xml", "src/test/resources/project-8/descriptor.xml",
                 "target/output-8-jsw" );

        File jswDir = getTestFile( "target/output-8-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        String dir = "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw";

        String testFile = dir + "/wrapper-8.conf";
        assertEquals( "Comparing " + testFile + " to " + wrapper,
                      normalizeLineTerminators( FileUtils.fileRead( getTestFile( testFile ) ) ),
                      normalizeLineTerminators( FileUtils.fileRead( wrapper ) ) );

        File shellScript = new File( jswDir, "bin/app" );

        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );

        testFile = dir + "/run-8.sh";
        assertEquals( "Comparing " + testFile + " to " + shellScript,
                      normalizeLineTerminators( FileUtils.fileRead( getTestFile( testFile ) ) ),
                      normalizeLineTerminators( FileUtils.fileRead( shellScript ) ) );

        File batchFile = new File( jswDir, "bin/app.bat" );

        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );

        testFile = dir + "/run-8.bat";
        assertEquals( "Comparing " + testFile + " to " + batchFile, FileUtils.fileRead( getTestFile( testFile ) ),
                      FileUtils.fileRead( batchFile ) );
    }

    public void testGenerationWithChkConfig()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-12/pom.xml", "src/test/resources/project-12/descriptor.xml",
                 "target/output-12-jsw" );

        File jswDir = getTestFile( "target/output-12-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        String wrapperContents = FileUtils.fileRead( wrapper );

        assertFalse( "Wrapper conf contains chkconfig.start", wrapperContents.contains( "chkconfig.start" ) );
        assertFalse( "Wrapper conf contains chkconfig.stop", wrapperContents.contains( "chkconfig.stop" ) );

        File script = new File( jswDir, "bin/app" );
        String scriptContents = FileUtils.fileRead( script );

        assertTrue( "Chkconfig replace did not work", scriptContents.contains( "chkconfig: 3 21 81" ) );
    }

    public void testGenerationWithWrapperPidFile()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-9/pom.xml", "src/test/resources/project-9/descriptor.xml",
                 "target/output-9-jsw" );

        File jswDir = getTestFile( "target/output-9-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        String dir = "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw";

        String testFile = dir + "/wrapper-9.conf";
        assertEquals( "Comparing " + testFile + " to " + wrapper,
                      normalizeLineTerminators( FileUtils.fileRead( getTestFile( testFile ) ) ),
                      normalizeLineTerminators( FileUtils.fileRead( wrapper ) ) );

        File shellScript = new File( jswDir, "bin/app" );

        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );

        testFile = dir + "/run-9.sh";
        assertEquals( "Comparing " + testFile + " to " + shellScript,
                      normalizeLineTerminators( FileUtils.fileRead( getTestFile( testFile ) ) ),
                      normalizeLineTerminators( FileUtils.fileRead( shellScript ) ) );

        // there are no changes in batch file so we don't check it here
    }

    public void testGenerationWithGoodExternalDeltaPack()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-10/pom.xml", "src/test/resources/project-10/descriptor.xml",
                 "target/output-10-jsw" );

        File jswDir = getTestFile( "target/output-10-jsw/app" );

        // just enough to prove we see the external files
        assertFileExists( jswDir, "lib/wrapper.jar" );
        assertFileExists( jswDir, "lib/libwrapper-aix-ppc-32.a" );
        assertFileExists( jswDir, "lib/libwrapper-aix-ppc-64.a" );

        // just enough to prove we see the external files
        assertFileExists( jswDir, "bin/wrapper-aix-ppc-32" );
        assertFileExists( jswDir, "bin/wrapper-aix-ppc-64" );
    }

    public void testGenerationWithBadExternalDeltaPack()
        throws Exception
    {
        try
        {
            runTest( "jsw", "src/test/resources/project-10/pom.xml", "src/test/resources/project-11/descriptor.xml",
                     "target/output-11-jsw" );

            fail( "Invalid external delta pack passed thru!!." );
        }
        catch ( DaemonGeneratorException e )
        {
            assertTrue( "Invalid external delta pack does not get correct exceptoin",
                        e.getMessage().startsWith( "Could not copy external file" ) );
        }
    }

    public void testGenerationWithRepositoryName()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-13/pom.xml", "src/test/resources/project-13/descriptor.xml",
                 "target/output-13-jsw" );

        File jswDir = getTestFile( "target/output-13-jsw/app" );
        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        String dir = "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw";

        String testFile = dir + "/wrapper-13.conf";
        assertEquals( "Comparing " + testFile + " to " + wrapper,
                      normalizeLineTerminators( FileUtils.fileRead( getTestFile( testFile ) ) ),
                      normalizeLineTerminators( FileUtils.fileRead( wrapper ) ) );

        assertFileExists( jswDir, "repo/wrapper.jar" );
        assertFileExists( jswDir, "repo/wrapper-windows-x86-32.dll" );
        assertFileExists( jswDir, "repo/wrapper-windows-x86-64.dll" );
        assertFileExists( jswDir, "repo/libwrapper-macosx-universal-32.jnilib" );
        assertFileExists( jswDir, "repo/libwrapper-linux-x86-32.so" );
        assertFileExists( jswDir, "repo/libwrapper-solaris-sparc-64.so" );
        assertFileNotExists( jswDir, "lib/wrapper.jar" );
        assertFileNotExists( jswDir, "lib/wrapper-windows-x86-32.dll" );
        assertFileNotExists( jswDir, "lib/wrapper-windows-x86-64.dll" );
        assertFileNotExists( jswDir, "lib/libwrapper-macosx-universal-32.jnilib" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-x86-32.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-solaris-sparc-64.so" );
    }

    public void testGenerationWithConfigurationDirectory()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-14/pom.xml", "src/test/resources/project-14/descriptor.xml",
                 "target/output-14-jsw" );

        File jswDir = getTestFile( "target/output-14-jsw/app" );
        assertFileExists( jswDir, "etc/wrapper.conf" );

        File shellScript = new File( jswDir, "bin/app" );
        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );
        assertEquals( FileUtils.fileRead( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-14.sh" ) ),
                      FileUtils.fileRead( shellScript ) );

        File batchFile = new File( jswDir, "bin/app.bat" );
        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );
        assertEquals( FileUtils.fileRead( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-14.bat" ) ),
                      FileUtils.fileRead( batchFile ) );
    }

    public void testGenerationWithEndorsedDirectory()
        throws Exception
    {
        runTest( "jsw", "src/test/resources/project-15/pom.xml", "src/test/resources/project-15/descriptor.xml",
                 "target/output-15-jsw" );

        File jswDir = getTestFile( "target/output-15-jsw/app" );
        assertFileExists( jswDir, "conf/wrapper.conf" );

        File wrapper = new File( jswDir, "conf/wrapper.conf" );

        String wrapperContents = FileUtils.fileRead( wrapper );

        assertTrue( "Wrapper conf does not contain lib-endorsed", wrapperContents.contains( "lib-endorsed" ) );
    }

    private static void assertFileExists( File jswDir, String file )
    {
        File wrapperJar = new File( jswDir, file );

        assertTrue( "File is missing: " + wrapperJar.getAbsolutePath(), wrapperJar.isFile() );
    }

    private static void assertFileNotExists( File jswDir, String file )
    {
        File wrapperJar = new File( jswDir, file );

        assertFalse( "File should be missing: " + wrapperJar.getAbsolutePath(), wrapperJar.isFile() );
    }
}
