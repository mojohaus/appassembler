package org.codehaus.mojo.appassembler;

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
