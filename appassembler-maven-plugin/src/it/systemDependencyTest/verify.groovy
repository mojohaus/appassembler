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
    def pom = new XmlSlurper().parse(new File(basedir, 'pom.xml'))
    
    def allPlugins = pom.build.plugins

    def dependencies = allPlugins.plugin

    def appassemblerModule = dependencies.find {
        item -> item.groupId.equals("org.codehaus.mojo") && item.artifactId.equals("appassembler-maven-plugin");
    }

   return appassemblerModule.version;
}
 
def projectVersion = getProjectVersion();

def buildLog = new File( basedir, "build.log")

t.checkExistenceAndContentOfAFile(buildLog, [
  '[ERROR] Failed to execute goal org.codehaus.mojo:appassembler-maven-plugin:' + projectVersion + ':assemble (default) on project systemDependency-test: The useAllDependencies has been marked as deprecated since version 1.3.1 -> [Help 1]',
  'org.apache.maven.lifecycle.LifecycleExecutionException: Failed to execute goal org.codehaus.mojo:appassembler-maven-plugin:' + projectVersion + ':assemble (default) on project systemDependency-test: The useAllDependencies has been marked as deprecated since version 1.3.1',
]);

return true;
