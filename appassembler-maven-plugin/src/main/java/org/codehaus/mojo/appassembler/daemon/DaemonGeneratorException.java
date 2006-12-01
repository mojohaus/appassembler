package org.codehaus.mojo.appassembler.daemon;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DaemonGeneratorException
    extends Exception
{
    public DaemonGeneratorException( String message )
    {
        super( message );
    }

    public DaemonGeneratorException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
