package org.codehaus.mojo.appassembler.daemon.script;

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
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component
 */
public class DefaultScriptGenerator
    extends AbstractLogEnabled
    implements ScriptGenerator
{
    // -----------------------------------------------------------------------
    // ScriptGenerator Implementation
    // -----------------------------------------------------------------------

    public void createBinScript( String platformName, Daemon daemon, File outputDirectory )
        throws DaemonGeneratorException
    {
        Platform platform = Platform.getInstance( platformName );

        InputStream in = null;

        FileWriter out = null;

        try
        {
            in = getClass().getResourceAsStream( platformName + "BinTemplate" );

            if ( in == null )
            {
                throw new DaemonGeneratorException(
                    "Internal error: could not find template for platform '" + platformName + "'." );
            }

            InputStreamReader reader = new InputStreamReader( in );

            Map context = new HashMap();
            context.put( "MAINCLASS", daemon.getMainClass() );
            context.put( "CLASSPATH", platform.getClassPath( daemon ) );
            context.put( "EXTRA_JVM_ARGUMENTS", platform.getExtraJvmArguments( daemon.getJvmSettings() ) );
            context.put( "APP_NAME", daemon.getId() );
            context.put( "ENV_SETUP", platform.getEnvSetup( daemon ) );
            context.put( "REPO", daemon.getRepositoryName() );
            if ( platform.isShowConsoleWindow( daemon ) )
            {
                context.put( "JAVA_BINARY", "java" );
            }
            else
            {
                context.put( "JAVA_BINARY", "start /min javaw" );
            }

            String appArguments = platform.getAppArguments( daemon );
            if ( appArguments != null )
            {
                context.put( "APP_ARGUMENTS", appArguments + " " );
            }
            else
            {
                context.put( "APP_ARGUMENTS", "" );
            }

            String interpolationToken = platform.getInterpolationToken();
            InterpolationFilterReader interpolationFilterReader =
                new InterpolationFilterReader( reader, context, interpolationToken, interpolationToken );

            // Set the name of the bin file
            String programName = "";

            if ( daemon.getId() == null || daemon.getId().trim().equals( "" ) )
            {
                // Get class name and use it as the filename
                StringTokenizer tokenizer = new StringTokenizer( daemon.getMainClass(), "." );
                while ( tokenizer.hasMoreElements() )
                {
                    programName = tokenizer.nextToken();
                }

                programName = programName.toLowerCase();
            }
            else
            {
                programName = daemon.getId();
            }

            File binDir = new File( outputDirectory, "bin" );
            FileUtils.forceMkdir( binDir );
            File binFile = new File( binDir, programName + platform.getBinFileExtension() );

            out = new FileWriter( binFile );
            getLogger().debug( "Writing shell file for platform '" + platform.getName() + "' to '"
                + binFile.getAbsolutePath() + "'." );

            IOUtil.copy( interpolationFilterReader, out );
        }
        catch ( FileNotFoundException e )
        {
            throw new DaemonGeneratorException( "Failed to get template for bin file.", e );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Failed to write bin file.", e );
        }
        finally
        {
            IOUtil.close( out );
            IOUtil.close( in );
        }
    }
}
