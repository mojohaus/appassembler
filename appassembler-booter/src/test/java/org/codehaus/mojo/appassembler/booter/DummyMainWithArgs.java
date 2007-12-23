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

public class DummyMainWithArgs
{

    public static void main( String[] args )
        throws Exception
    {
        if ( args.length != 2 )
        {
            throw new Exception( "Expected two arguments, but got " + args.length );
        }

        if ( !"first argument".equals( args[0] ) )
        {
            throw new Exception( "Expected first argument to be 'first argument' but was " + args[0] );
        }

        if ( !"second argument".equals( args[1] ) )
        {
            throw new Exception( "Expected second argument to be 'second argument' but was " + args[1] );
        }
    }

}
