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

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/generated-resources/appassembler/jsw/daemon-1/bin/");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "daemon-1" );

t.checkExistenceAndContentOfAFile(unixScriptFile, [
    'APP_NAME="daemon-1"',
    'APP_LONG_NAME="Test Project"',
])

//conf folder.
def fileConfFolder = new File( basedir, "target/generated-resources/appassembler/jsw/daemon-1/conf/");


// Check the existence of the generated wrapper.conf file.
def wrapperConfFile = new File( fileConfFolder, "wrapper.conf" );

t.checkExistenceAndContentOfAFile(wrapperConfFile, [
    'wrapper.java.classpath.1=lib/wrapper.jar',
    'wrapper.java.classpath.2=%REPO_DIR%/project-7-1.0-SNAPSHOT.jar',
    'wrapper.java.classpath.3=%REPO_DIR%/appassembler-model-' + projectVersion + '.jar',
    'wrapper.java.classpath.4=%REPO_DIR%/plexus-utils-1.5.6.jar',
    'wrapper.java.classpath.5=%REPO_DIR%/stax-api-1.0.1.jar',
    'wrapper.java.classpath.6=%REPO_DIR%/stax-utils-20060502.jar',
    'wrapper.java.classpath.7=%REPO_DIR%/stax-1.1.1-dev.jar',
    'wrapper.java.classpath.8=%REPO_DIR%/junit-3.8.1.jar',
])

return true;
