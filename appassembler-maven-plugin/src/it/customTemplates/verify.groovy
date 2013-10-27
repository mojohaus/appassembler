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

def daemonFolder = new File( basedir, "target/appassembler/jsw/daemon-1/")

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( daemonFolder, "bin");

// Check the existence of the generated scripts
def unixJswScriptFile = new File( fileBinFolder, "daemon-1" );

t.checkExistenceAndContentOfAFile(unixJswScriptFile, [
	'# From unixJswScriptTemplate',
	'APP_NAME="daemon-1"',
    'APP_LONG_NAME="Daemon Test Project"',
])

def unixScriptFile = new File( fileBinFolder, "script-1" );

t.checkExistenceAndContentOfAFile(unixScriptFile, [
	'#from unixScriptTemplate',
])

// Check the existence of the generated scripts
def windowsJswScriptFile = new File( fileBinFolder, "daemon-1.bat" );

t.checkExistenceAndContentOfAFile(windowsJswScriptFile, [
	'rem from windowsJswScriptTemplate',
])

def windowsScriptFile = new File( fileBinFolder, "script-1.bat" );

t.checkExistenceAndContentOfAFile(windowsScriptFile, [
    'rem from windowsScriptTemplate',
])


//conf folder.
def fileConfFolder = new File( daemonFolder, "conf");

// Check the existence of the generated wrapper.conf file.
def wrapperConfFile = new File( fileConfFolder, "wrapper.conf" );

t.checkExistenceAndContentOfAFile(wrapperConfFile, [
    'wrapper.java.classpath.1=lib/wrapper.jar',
    'wrapper.java.classpath.2=%REPO_DIR%/customTemplates-1.0-SNAPSHOT.jar',
    'wrapper.java.library.path.1=lib',
])

/////////////////////////////////////////////////////////////////////////////////

def daemonFolder2 = new File( basedir, "target/appassembler/jsw/daemon-2/")

//The bin folder where to find the generated scripts.
def fileBinFolder2 = new File( daemonFolder2, "bin");

// Check the existence of the generated scripts
def unixJswScriptFile2 = new File( fileBinFolder2, "daemon-2" );

t.checkExistenceAndContentOfAFile(unixJswScriptFile2, [
    '# From unixJswScriptTemplateSample'
])

def unixScriptFile2 = new File( fileBinFolder2, "script-2" );

t.checkExistenceAndContentOfAFile(unixScriptFile2, [
    '#from unixScriptTemplateSample',
])

// Check the existence of the generated scripts
def windowsJswScriptFile2 = new File( fileBinFolder2, "daemon-2.bat" );

t.checkExistenceAndContentOfAFile(windowsJswScriptFile2, [
	'rem from windowsJswScriptTemplateSample',
])

def windowsScriptFile2 = new File( fileBinFolder2, "script-2.bat" );

t.checkExistenceAndContentOfAFile(windowsScriptFile2, [
    'rem from windowsScriptTemplateSample',
])


//conf folder.
def fileConfFolder2 = new File( daemonFolder2, "conf");

// Check the existence of the generated wrapper.conf file.
def wrapperConfFile2 = new File( fileConfFolder2, "wrapper.conf" );

t.checkExistenceAndContentOfAFile(wrapperConfFile2, [
    'wrapper.java.classpath.1=lib/wrapper.jar',
    'wrapper.java.classpath.2=%REPO_DIR%/customTemplates-1.0-SNAPSHOT.jar',
    'wrapper.java.library.path.1=lib',
])


return true;
