/**
 *
 * The MIT License
 *
 * Copyright 2006-2013 The Codehaus.
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

import groovy.util.XmlSlurper


t = new IntegrationBase();

def repoFolder = new File( basedir, "target/appassembler/repo/");

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

def filesInRepository = [
 new File( repoFolder, "junit.jar"),
 new File( repoFolder, "stax-utils.jar"),
 new File( repoFolder, "appassembler-booter.jar"),
 new File( repoFolder, "appassembler-model.jar"),
 new File( repoFolder, "mappasm-71-2.jar"),
 new File( repoFolder, "plexus-utils.jar"),
 new File( repoFolder, "stax.jar"),
 new File( repoFolder, "stax-api.jar"),
]

filesInRepository.each {
  fileInRepository -> if (!fileInRepository.canRead()) {
    throw new FileNotFoundException("Could not find " + fileInRepository + " in generated repository.");
  }
}

return true;
