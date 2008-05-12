package org.codehaus.mojo.appassembler.daemon.merge;

/*
 * The MIT License
 *
 * Copyright 2005-2007 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.mojo.appassembler.model.Classpath;
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
        result.setClasspath( (Classpath) select( dominant.getClasspath(), recessive.getClasspath() ) );
        result.setCommandLineArguments(
            select( dominant.getCommandLineArguments(), recessive.getCommandLineArguments() ) );
        // This should probably be improved
        result.setJvmSettings( (JvmSettings) select( dominant.getJvmSettings(), recessive.getJvmSettings() ) );
        result.setShowConsoleWindow( dominant.isShowConsoleWindow() );

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
