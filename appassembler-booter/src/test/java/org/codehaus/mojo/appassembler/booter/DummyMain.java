package org.codehaus.mojo.appassembler.booter;

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

import java.net.URL;

public class DummyMain
{
    public static boolean kickAss;

    public static void main( String[] args )
        throws Exception
    {
        String foo = System.getProperty( "foo" );

        if ( foo == null || !foo.equals( "bar" ) )
        {
            throw new Exception( "System propertyp 'foo' was expected to be 'bar', was: " + foo );
        }

        String bar = System.getProperty( "bar" );

        if ( bar == null || !bar.equals( "foo" ) )
        {
            throw new Exception( "System propertyp 'bar' was expected to be 'foo', was: " + bar );
        }

        URL resource = DummyMain.class.getResource( "/org/codehaus/mojo/appassembler/booter/app.xml" );

        if ( resource.toExternalForm().indexOf( "org/codehaus/mojo/appassembler" ) == -1 )
        {
            throw new Exception( "Expected url to '/app.xml' to contain 'org/codehaus/mojo/appassembler'. Was: " +
                resource.toExternalForm() );
        }

        kickAss = true;
    }
}
