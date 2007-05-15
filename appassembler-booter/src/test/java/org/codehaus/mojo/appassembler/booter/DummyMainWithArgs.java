package org.codehaus.mojo.appassembler.booter;


public class DummyMainWithArgs
{

    public static void main( String[] args ) throws Exception
    {
        if ( args.length != 2 )
            throw new Exception( "Expected two arguments, but got " + args.length );

        if ( !"first argument".equals( args[0] ) )
            throw new Exception( "Expected first argument to be 'first argument' but was " + args[0] );

        if ( !"second argument".equals( args[1] ) )
            throw new Exception( "Expected second argument to be 'second argument' but was " + args[1] );
    }

}
