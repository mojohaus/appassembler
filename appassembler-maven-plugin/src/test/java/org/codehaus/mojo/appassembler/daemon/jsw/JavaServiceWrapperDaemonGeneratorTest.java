package org.codehaus.mojo.appassembler.daemon.jsw;

/*
 * The MIT License
 *
 * Copyright 2005-2007 The Codehaus.
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

import org.codehaus.mojo.appassembler.daemon.AbstractDaemonGeneratorTest;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo test could be improved - there are other conditions like "check if extra properties can override those from template"
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

        assertEquals( FileUtils.fileRead(
            getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/wrapper-1.conf" ) ),
                      FileUtils.fileRead( wrapper ) );

        File shellScript = new File( jswDir, "bin/app" );

        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );

        assertEquals( FileUtils.fileRead(
            getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-1.sh" ) ),
                      FileUtils.fileRead( shellScript ) );

        File batchFile = new File( jswDir, "bin/app.bat" );

        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );

        assertEquals( FileUtils.fileRead(
            getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-1.bat" ) ),
                      FileUtils.fileRead( batchFile ) );

        assertEquals( ( new File( getBasedir() +
            "/target/classes/org/codehaus/mojo/appassembler/daemon/jsw/bin/wrapper-linux-x86-32" ) ).length(),
                      ( new File( jswDir, "bin/wrapper-linux-x86-32" )).length() );

        assertFileExists( jswDir, "lib/wrapper.jar" );
        assertFileExists( jswDir, "lib/wrapper-windows-x86-32.dll" );
        assertFileExists( jswDir, "lib/libwrapper-macosx-universal-32.jnilib" );
        assertFileNotExists( jswDir, "lib/libwrapper-macosx-ppc-32.jnilib" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-ppc-64.so" );
        assertFileExists( jswDir, "lib/libwrapper-linux-x86-32.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-linux-x86-64.so" );
        assertFileExists( jswDir, "lib/libwrapper-solaris-x86-32.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-solaris-sparc-32.so" );
        assertFileNotExists( jswDir, "lib/libwrapper-solaris-sparc-64.so" );

        assertFileExists( jswDir, "bin/wrapper-windows-x86-32.exe" );
        assertFileExists( jswDir, "bin/wrapper-macosx-universal-32" );
        assertFileNotExists( jswDir, "bin/wrapper-macosx-ppc-32" );
        assertFileNotExists( jswDir, "bin/wrapper-linux-ppc-64" );
        assertFileExists( jswDir, "bin/wrapper-linux-x86-32" );
        assertFileNotExists( jswDir, "bin/wrapper-linux-x86-64" );
        assertFileExists( jswDir, "bin/wrapper-solaris-x86-32" );
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
        
        assertEquals( FileUtils.fileRead(
           getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/wrapper-1.conf" ) ),
                     FileUtils.fileRead( wrapper ) );
        
        File shellScript = new File( jswDir, "bin/app" );
        
        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );
        
        assertEquals( FileUtils.fileRead(
           getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-1.sh" ) ),
                     FileUtils.fileRead( shellScript ) );
        
        File batchFile = new File( jswDir, "bin/app.bat" );
        
        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );
        
        assertEquals( FileUtils.fileRead(
           getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/run-1.bat" ) ),
                     FileUtils.fileRead( batchFile ) );
        
        assertEquals( ( new File( getBasedir() +
           "/target/classes/org/codehaus/mojo/appassembler/daemon/jsw/bin/wrapper-linux-x86-32" ) ).length(),
                     ( new File( jswDir, "bin/wrapper-linux-x86-32" )).length() );
        
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
        
        assertEquals( FileUtils.fileRead(
           getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/no-generator-configurations-wrapper.conf" ) ),
                     FileUtils.fileRead( wrapper ) );
        
        File shellScript = new File( jswDir, "bin/app" );
        
        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );
        
        assertEquals( FileUtils.fileRead(
           getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/no-generator-configurations-run.sh" ) ),
                     FileUtils.fileRead( shellScript ) );
        
        File batchFile = new File( jswDir, "bin/app.bat" );
        
        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );
        
        assertEquals( FileUtils.fileRead(
           getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/no-generator-configurations-run.bat" ) ),
                     FileUtils.fileRead( batchFile ) );
        
        assertEquals( ( new File( getBasedir() +
           "/target/classes/org/codehaus/mojo/appassembler/daemon/jsw/bin/wrapper-linux-x86-32" ) ).length(),
                     ( new File( jswDir, "bin/wrapper-linux-x86-32" )).length() );
        
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

        assertEquals( FileUtils.fileRead(
           getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/jsw/wrapper-6.conf" ) ),
                     FileUtils.fileRead( wrapper ) );
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
        assertEquals( "Comparing " + testFile + " to " + wrapper, normalizeLineTerminators( FileUtils.fileRead( getTestFile( testFile ) ) ),
                      normalizeLineTerminators( FileUtils.fileRead( wrapper ) ) );

        File shellScript = new File( jswDir, "bin/app" );

        assertTrue( "Shell script file is missing: " + shellScript.getAbsolutePath(), shellScript.isFile() );

        testFile = dir + "/run-8.sh";
        assertEquals( "Comparing " + testFile + " to " + shellScript, normalizeLineTerminators( FileUtils.fileRead( getTestFile( testFile ) ) ),
                      normalizeLineTerminators( FileUtils.fileRead( shellScript ) ) );

        File batchFile = new File( jswDir, "bin/app.bat" );

        assertTrue( "Batch file is missing: " + batchFile.getAbsolutePath(), batchFile.isFile() );

        testFile = dir + "/run-8.bat";
        assertEquals( "Comparing " + testFile + " to " + batchFile, FileUtils.fileRead( getTestFile( testFile ) ),
                      FileUtils.fileRead( batchFile ) );
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
