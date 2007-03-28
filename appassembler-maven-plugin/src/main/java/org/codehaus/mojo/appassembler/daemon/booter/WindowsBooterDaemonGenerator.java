package org.codehaus.mojo.appassembler.daemon.booter;

/**
 * Generates unix and/or windows wrapperscripts.
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @plexus.component role-hint="booter-windows"
 */
public class WindowsBooterDaemonGenerator
    extends AbstractBooterDaemonGenerator
{
    public WindowsBooterDaemonGenerator()
    {
        super( true );
    }
}
