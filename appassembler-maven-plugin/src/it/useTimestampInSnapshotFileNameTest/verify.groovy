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

//Check the existence of the generated repository.
def repoFolder1 = new File( basedir, "target/exec-1/repo");
def jarFile1 = new File( repoFolder1, "dbupgrade-core-1.0-beta-3-SNAPSHOT.jar" );

if ( jarFile1.canRead() ) {
    throw new RuntimeException( "Unexpected generated jar found! " + jarFile1 );
}

def repoFolder2 = new File( basedir, "target/exec-2/repo");
def jarFile2 = new File( repoFolder2, "dbupgrade-core-1.0-beta-3-SNAPSHOT.jar" );

if ( !jarFile2.canRead() ) {
    throw new FileNotFoundException( "Generated jar found! " + jarFile1 );
}

return true;
