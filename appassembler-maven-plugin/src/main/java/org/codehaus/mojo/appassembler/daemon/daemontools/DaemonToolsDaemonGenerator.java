package org.codehaus.mojo.appassembler.daemon.daemontools;

import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role-hint="daemontools"
 */
public class DaemonToolsDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{
    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    public void generate( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        Daemon daemon = request.getDaemon();

        try
        {
            FileUtils.forceMkdir( request.getOutputDirectory() );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error creating output directory: " + request.getOutputDirectory(), e );
        }

        File envDir = new File( request.getOutputDirectory(), "env" );
        envDir.mkdir();

        copyEnvFile( "JAVA_HOME", envDir );
        copyEnvFile( "USER", envDir );

        File logDir = new File( request.getOutputDirectory(), "logs" );
        logDir.mkdir();

        File serviceDir = new File( request.getOutputDirectory(), "service" );
        serviceDir.mkdir();

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        InputStream in = this.getClass().getResourceAsStream( "run.sh.template" );

        if ( in == null )
        {
            throw new DaemonGeneratorException( "Could not load template." );
        }

        InputStreamReader reader = new InputStreamReader( in );

        Map context = new HashMap();
        context.put( "MAINCLASS", daemon.getMainClass() );
        context.put( "NAME", daemon.getId() );

        InterpolationFilterReader interpolationFilterReader = new InterpolationFilterReader( reader, context, "@", "@" );

        File runFile = new File( request.getOutputDirectory(), "run" );
        FileWriter out = null;

        try
        {
            // -----------------------------------------------------------------------
            // Write the file
            // -----------------------------------------------------------------------

            out = new FileWriter( runFile );

            IOUtil.copy( interpolationFilterReader, out );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error writing output file: " + runFile.getAbsolutePath(), e );
        }
        finally
        {
            IOUtil.close(interpolationFilterReader);
            IOUtil.close(out);
        }

    }

    private void copyEnvFile( String envName, File envDir )
        throws DaemonGeneratorException
    {
        Writer out = null;
        Reader envReader = null;

        File envFile = new File( envDir, envName );

        try
        {
            envReader = new InputStreamReader( this.getClass().getResourceAsStream( "env/" + envName ) );

            // -----------------------------------------------------------------------
            // Write the file
            // -----------------------------------------------------------------------

            out = new FileWriter( envFile );

            IOUtil.copy( envReader, out );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error writing environment file: " + envFile, e );
        }
        finally
        {
            IOUtil.close(envReader);
            IOUtil.close(out);
        }
    }
}