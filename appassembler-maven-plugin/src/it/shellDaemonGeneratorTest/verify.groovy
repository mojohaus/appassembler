/**
 *
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

import java.io.*
import java.util.*

import org.codehaus.mojo.appassembler.daemon.standard.UnixScriptDaemonGenerator;

import groovy.util.XmlSlurper


t = new IntegrationBase();

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin/");

/**
* This will filter out the project version out of the
* pom.xml file, cause currently no opportunity exists to
* get this information via Maven Invoker Plugin into
* the Groovy script code.
* @return Version information.
*/
def getProjectVersion() {
   def pom = new XmlSlurper().parse(new File(basedir, 'pom.xml'))

   def allDependencies = pom.dependencies;

   def dependencies = allDependencies.dependency

   def appassemblerModule = dependencies.find {
       item -> item.groupId.equals("org.codehaus.mojo.appassembler") && item.artifactId.equals("appassembler-model");
   }

   return appassemblerModule.version;
}

def projectVersion = getProjectVersion();

def unix_line = [
    'CLASSPATH="\$BASEDIR"/etc',
    '"\$REPO"/org/codehaus/plexus/plexus-utils/1.1/plexus-utils-1.1.jar',
    '"\$REPO"/org/codehaus/mojo/appassembler/appassembler-booter/' + projectVersion + '/appassembler-booter-' + projectVersion + '.jar',
    '"\$REPO"/stax/stax-api/1.0.1/stax-api-1.0.1.jar',
    '"\$REPO"/org/codehaus/mojo/appassembler/appassembler-model/' + projectVersion + '/appassembler-model-' + projectVersion + '.jar',
    '"\$REPO"/net/java/dev/stax-utils/stax-utils/20070216/stax-utils-20070216.jar',
    '"\$REPO"/stax/stax/1.1.2-dev/stax-1.1.2-dev.jar',
    '"\$REPO"/junit/junit/3.8.1/junit-3.8.1.jar',
    '"\$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/shellDaemonGeneratorTest/1.0-SNAPSHOT/shellDaemonGeneratorTest-1.0-SNAPSHOT.jar',
]

// Check the existence of the generated unix script
File unixScriptFile = new File( fileBinFolder, "app" );
assert unixScriptFile.exists()

unix_line.each {
    line -> assert unixScriptFile.getText().count( line ) == 1;
}

def windows_line = [
    'set CLASSPATH="%BASEDIR%"\\etc',
    '"%REPO%"\\org\\codehaus\\plexus\\plexus-utils\\1.1\\plexus-utils-1.1.jar',
    "\"%REPO%\"\\org\\codehaus\\mojo\\appassembler\\appassembler-booter\\${projectVersion}\\appassembler-booter-${projectVersion}.jar",
    '"%REPO%"\\stax\\stax-api\\1.0.1\\stax-api-1.0.1.jar',
    "\"%REPO%\"\\org\\codehaus\\mojo\\appassembler\\appassembler-model\\${projectVersion}\\appassembler-model-${projectVersion}.jar",
    '"%REPO%"\\net\\java\\dev\\stax-utils\\stax-utils\\20070216\\stax-utils-20070216.jar',
    '"%REPO%"\\stax\\stax\\1.1.2-dev\\stax-1.1.2-dev.jar',
    '"%REPO%"\\junit\\junit\\3.8.1\\junit-3.8.1.jar',
    '"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\shellDaemonGeneratorTest\\1.0-SNAPSHOT\\shellDaemonGeneratorTest-1.0-SNAPSHOT.jar',
]

// Check the existence of the generated windows batch file.
File windowsBatchFile = new File( fileBinFolder, "app.bat" );
assert windowsBatchFile.exists()

windows_line.each {
    line -> assert windowsBatchFile.getText().count( line ) == 1;
}


return true;
