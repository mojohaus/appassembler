package org.codehaus.mojo.appassembler.example.helloworld;

public class HelloWorld
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        System.out.println( "Number of command line arguments: " + args.length );

        for ( int i = 0; i < args.length; i++ )
        {
            System.out.println( "Argument #" + i + ":" + args[ i ] );
        }

        System.out.println( "basedir: " + System.getProperty( "basedir" ) );
    }
}
