package org.codehaus.mojo.appassembler.daemon.merge;

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface DaemonMerger
{
    String ROLE = DaemonMerger.class.getName();

    Daemon mergeDaemons( Daemon dominant, Daemon recessive )
        throws DaemonGeneratorException;
}
