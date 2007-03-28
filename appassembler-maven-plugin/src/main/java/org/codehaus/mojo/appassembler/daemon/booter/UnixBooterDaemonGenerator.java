package org.codehaus.mojo.appassembler.daemon.booter;

/**
 * Generates unix and/or windows wrapperscripts.
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @plexus.component role-hint="booter-unix"
 */
public class UnixBooterDaemonGenerator
    extends AbstractBooterDaemonGenerator
{
    public UnixBooterDaemonGenerator()
    {
        super( false );
    }
}
