package org.codehaus.mojo.appassembler.daemon.booter;

import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;

/**
 * Generates unix and/or windows wrapperscripts.
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @plexus.component role-hint="booter-unix"
 */
public class UnixBooterDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{
    /**
     * @plexus.requirement role-hint="generic"
     */
    DaemonGenerator genericDaemonGenerator;

    public void generate( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        ScriptUtils.createBinScript( false,
                                     request.getDaemon(),
                                     request.getMavenProject(),
                                     request.getLocalRepository(),
                                     request.getOutputDirectory() );

        request.setOutputDirectory( new File( request.getOutputDirectory(), "etc" ) );

        genericDaemonGenerator.generate( request  );
    }
}
