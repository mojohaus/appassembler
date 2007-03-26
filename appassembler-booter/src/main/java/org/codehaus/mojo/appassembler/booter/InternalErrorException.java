package org.codehaus.mojo.appassembler.booter;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class InternalErrorException
    extends Exception
{
    public InternalErrorException( String message )
    {
        super( message );
    }

    public InternalErrorException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
