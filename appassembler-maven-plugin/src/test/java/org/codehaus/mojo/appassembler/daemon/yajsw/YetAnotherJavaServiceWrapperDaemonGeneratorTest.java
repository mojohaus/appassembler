/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codehaus.mojo.appassembler.daemon.yajsw;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import org.codehaus.mojo.appassembler.daemon.AbstractDaemonGeneratorTest;
import static org.codehaus.plexus.PlexusTestCase.getBasedir;
import static org.codehaus.plexus.PlexusTestCase.getTestFile;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Sandra Parsick
 * @version $Id$
 */

public class YetAnotherJavaServiceWrapperDaemonGeneratorTest
        extends AbstractDaemonGeneratorTest
{

    public void testDefaultYAJSWFilesIfNoGeneratorConfigurationsIsSet()
            throws Exception
    {
        runTest( "yajsw", "src/test/resources/project-5/pom.xml", "src/test/resources/project-5/descriptor.xml",
                "target/output-5-yajsw" );

        File yajswDir = getTestFile( "target/output-5-yajsw/app" );
        File wrapper = new File( yajswDir, "conf/wrapper.conf" );

        assertTrue( "Wrapper file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile() );

        assertEquals( normalizedLineEndingRead( "src/test/resources/org/codehaus/mojo/appassembler/daemon/yajsw/wrapper-5.conf" ),
                trimWhitespacePerLine(wrapper));

        assertFileExists(yajswDir, "bat/runConsole.bat");
        assertFileExists(yajswDir, "bat/startService.bat");
        assertFileExists(yajswDir, "bat/stopService.bat");
        assertFileExists(yajswDir, "bat/uninstallService.bat");
        assertFileExists(yajswDir, "bat/setenv.bat");
        assertFileExists(yajswDir, "bat/wrapper.bat");
        assertFileExists(yajswDir, "bin/runConsole.sh");
        assertFileExists(yajswDir, "bin/installDaemon.sh");
        assertFileExists(yajswDir, "bin/startDaemon.sh");
        assertFileExists(yajswDir, "bin/stopDaemon.sh");
        assertFileExists(yajswDir, "bin/uninstallDaemon.sh");
        assertFileExists(yajswDir, "bin/setenv.sh");
        assertFileExists(yajswDir, "bin/wrapper.sh");
        assertFileExists(yajswDir, "bin/installDaemonNoPriv.sh");
        assertFileExists(yajswDir, "bin/uninstallDaemonNoPriv.sh");
        assertFileExists(yajswDir, "bin/startDaemonNoPriv.sh");
        assertFileExists(yajswDir, "bin/stopDaemonNoPriv.sh");
        assertFileExists( yajswDir, "wrapper.jar" );
        assertFileExists( yajswDir, "wrapperApp.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-cli-1.4.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-collections-3.2.2.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-configuration2-2.3.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-io-2.6.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-lang3-3.8.1.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-lang-2.6.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-logging-1.1.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-text-1.6.jar" );
        assertFileExists( yajswDir, "lib/core/commons/commons-vfs2-2.2.jar" );
        assertFileExists( yajswDir, "lib/core/jna/jna-5.3.1.jar");
        assertFileExists( yajswDir, "lib/core/jna/jna-platform-5.3.1.jar");
        assertFileExists( yajswDir, "lib/core/jna/jna-platform-5.3.1.jar");
        assertFileExists( yajswDir, "lib/core/netty/netty-all-4.1.36.Final.jar");
        assertFileExists( yajswDir, "lib/core/yajsw/ahessian.jar");
        assertFileExists( yajswDir, "lib/extended/groovy/groovy-2.5.7.jar");
        assertFileExists( yajswDir, "lib/extended/groovy/groovy-patch.jar");
        assertFileExists( yajswDir, "lib/extended/velocity/velocity-1.7.jar");
        assertFileExists( yajswDir, "lib/extended/yajsw/hessian4.jar");
        assertFileExists( yajswDir, "scripts/trayMessage.gv");
        assertFileExists(yajswDir, "templates/daemon.vm");
    }

    private String trimWhitespacePerLine(File wrapper) throws IOException {
        List<String> readLines = org.apache.commons.io.FileUtils.readLines(wrapper);

        StringBuilder file = new StringBuilder();
        for(String line : readLines) {
            String trimmedLine = line.trim();
            file.append(trimmedLine);
            file.append("\n");
        }
        return file.toString();
    }

    private String normalizedLineEndingRead( String file )
            throws IOException
    {
        String expected = FileUtils.fileRead( getTestFile( file ) );
        return StringUtils.unifyLineSeparators( expected );
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
