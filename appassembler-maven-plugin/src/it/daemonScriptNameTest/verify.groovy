/**
 *
 * The MIT License
 *
 * Copyright 2006-2011 The Codehaus.
 * Copyright 2015 The MojoHaus.
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


class CheckFolder {
  void checkExistingScripts(File folder) {
    //The bin folder where to find the generated scripts.
    def fileBinFolder = new File( folder, "bin");

    // Check the existence of the generated unix script
    def unixScriptFile = new File( fileBinFolder, "executable" );
    def windowsScriptFile = new File( fileBinFolder, "executable.bat" );

    if (unixScriptFile.exists()) {
      throw new FileNotFoundException("The file " + unixScriptFile + " does not exist.");
    }
    if (windowsScriptFile.exists()) {
      throw new FileNotFoundException("The file " + windowsScriptFile + " does not exist.");
    }
  }
}


t = new CheckFolder();

def jswFolder = new File( basedir, "target/generated-resources/appassembler/jsw")
def daemon1Folder = new File( jswFolder, "daemon-1")

t.checkExistingScripts(daemon1Folder)

def daemon2Folder = new File( jswFolder, "daemon-2")
t.checkExistingScripts(daemon2Folder)

return true;
