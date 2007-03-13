package org.codehaus.mojo.appassembler.daemon.merge;

import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component
 */
public class DefaultDaemonMerger
    extends AbstractLogEnabled
    implements DaemonMerger
{
    // -----------------------------------------------------------------------
    // DaemonMerger Implementation
    // -----------------------------------------------------------------------

    public Daemon mergeDaemons( Daemon dominant, Daemon recessive )
        throws DaemonGeneratorException
    {
        if ( dominant == null )
        {
            return recessive;
        }

        if ( recessive == null )
        {
            return dominant;
        }

        Daemon result = new Daemon();

        result.setId( select( dominant.getId(), recessive.getId() ) );
        result.setMainClass( select( dominant.getMainClass(), recessive.getMainClass() ) );
        result.setClasspath( select( dominant.getClasspath(), recessive.getClasspath() ) );
        result.setCommandLineArguments(
            select( dominant.getCommandLineArguments(), recessive.getCommandLineArguments() ) );
        // This should probably be improved
        result.setJvmSettings( (JvmSettings) select( dominant.getJvmSettings(), recessive.getJvmSettings() ) );

        return result;
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private String select( String dominant, String recessive )
    {
        if ( StringUtils.isNotEmpty( dominant ) )
        {
            return dominant;
        }
        else
        {
            return recessive;
        }
    }

    private List select( List dominant, List recessive )
    {
        // Even if the list is empty, return it. This makes it possible to clear the default list.

        // TODO: The above is not possible as long as the modello generated stuff returns an empty list on not set fields.
        if ( dominant != null && dominant.size() > 0 )
        {
            return dominant;
        }
        else
        {
            return recessive;
        }
    }

    private Object select( Object dominant, Object recessive )
    {
        if ( dominant != null )
        {
            return dominant;
        }
        else
        {
            return recessive;
        }
    }
}
