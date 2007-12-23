package org.codehaus.mojo.appassembler;

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

import junit.framework.TestCase;

import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AssembleMojoTest
    extends TestCase
{
    public void testParseTokens()
    {
        List tokens = AssembleMojo.parseTokens( "" );
        assertEquals( 0, tokens.size() );

        tokens = AssembleMojo.parseTokens( null );
        assertEquals( 0, tokens.size() );

        tokens = AssembleMojo.parseTokens( "a b" );
        assertEquals( 2, tokens.size() );
        assertEquals( "a", tokens.get( 0 ) );
        assertEquals( "b", tokens.get( 1 ) );

        tokens = AssembleMojo.parseTokens( "\"a b\"" );
        assertEquals( 1, tokens.size() );
        assertEquals( "a b", tokens.get( 0 ) );

        tokens = AssembleMojo.parseTokens( "\"a b c\"" );
        assertEquals( 1, tokens.size() );
        assertEquals( "a b c", tokens.get( 0 ) );

        tokens = AssembleMojo.parseTokens( "123 \"a b c\" 321" );
        assertEquals( tokens.toString(), 3, tokens.size() );
        assertEquals( "123", tokens.get( 0 ) );
        assertEquals( "a b c", tokens.get( 1 ) );
        assertEquals( "321", tokens.get( 2 ) );
    }
}
