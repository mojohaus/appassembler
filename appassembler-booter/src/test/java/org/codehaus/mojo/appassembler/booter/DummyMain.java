package org.codehaus.mojo.appassembler.booter;

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
            throw new Exception( "Expected url to '/app.xml' to contain 'org/codehaus/mojo/appassembler'. Was: " + resource.toExternalForm() );
        }

        kickAss = true;
    }
}
