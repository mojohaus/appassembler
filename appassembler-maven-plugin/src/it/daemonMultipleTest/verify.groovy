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


t = new IntegrationBase();

/**
* This will filter out the project version out of the
* pom.xml file, cause currently no opportunity exists to
* get this information via Maven Invoker Plugin into
* the Groovy script code.
* @return Version information.
*/
def getProjectVersion() {
   def pom = new XmlSlurper().parse(new File('pom.xml'))
   def version = pom.childNodes().find {
       item -> item.name().equals("version")
   }
   return version.text()
}

def projectVersion = getProjectVersion();

def daemonFolder = new File( basedir, "target/generated-resources/appassembler/jsw/daemon-1/")

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( daemonFolder, "bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "daemon-1" );

t.checkExistenceAndContentOfAFile(unixScriptFile, [
    'APP_NAME="daemon-1"',
    'APP_LONG_NAME="Daemon Multiple Test Project"',
])

//conf folder.
def fileConfFolder = new File( daemonFolder, "conf");

// Check the existence of the generated wrapper.conf file.
def wrapperConfFile = new File( fileConfFolder, "wrapper.conf" );

//TODO: Add more testing ..
t.checkExistenceAndContentOfAFile(wrapperConfFile, [
    'wrapper.java.classpath.1=lib/wrapper.jar',
    'wrapper.java.classpath.2=%REPO_DIR%/org/codehaus/mojo/appassembler/daemonMultipleTest/1.0-SNAPSHOT/daemonMultipleTest-1.0-SNAPSHOT.jar',
    'wrapper.java.classpath.3=%REPO_DIR%/log4j/log4j/1.2.14/log4j-1.2.14.jar',
])

return true;
