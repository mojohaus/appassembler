package org.codehaus.mojo.appassembler.daemon.booter;

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
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @version $Id$
 */
public class ShellDaemonGeneratorTest
    extends AbstractDaemonGeneratorTest
{
    public void testGenerateWindowsShellDaemon()
        throws Exception
    {
        runTest( "booter-windows", "src/test/resources/project-3/pom.xml",
                 "src/test/resources/project-1/descriptor.xml", "target/output-3-bat" );

        File wrapper = new File( getTestFile( "target/output-3-bat" ), "bin/app.bat" );

        assertTrue( "Windows batch file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        assertEquals( "Generated batch file does not match template", FileUtils.fileRead(
            createFilteredFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/booter/app.bat" ) ),
                                                                      FileUtils.fileRead( wrapper ) );

        File manifest = new File( getTestFile( "target/output-3-bat" ), "etc/app.xml" );

        assertTrue( "Manifest file is mising: " + manifest.getAbsolutePath(), manifest.isFile() );
    }

    public void testGenerateUnixShellDaemon()
        throws Exception
    {
        runTest( "booter-unix", "src/test/resources/project-3/pom.xml", "src/test/resources/project-1/descriptor.xml",
                 "target/output-3-sh" );

        File wrapper = new File( getTestFile( "target/output-3-sh" ), "bin/app" );

        assertTrue( "Unix shell script is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        assertEquals( "Generated batch file does not match template", FileUtils.fileRead(
            createFilteredFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/booter/app" ) ),
                                                                      FileUtils.fileRead( wrapper ) );

        File manifest = new File( getTestFile( "target/output-3-bat" ), "etc/app.xml" );

        assertTrue( "Manifest file is mising: " + manifest.getAbsolutePath(), manifest.isFile() );
    }
}
