package org.codehaus.mojo.appassembler.example.helloworld;

import java.io.FileOutputStream;
import java.util.Properties;

public class HelloWorld
{
    public static void main( String[] args )
        throws Exception
    {
        // This tests that arguments are passes correctly and make it independent of any other settings
        FileOutputStream out = new FileOutputStream( args[0] );
        System.getProperties().store( out, null );
        out.close();

        System.exit(0);
    }
}
