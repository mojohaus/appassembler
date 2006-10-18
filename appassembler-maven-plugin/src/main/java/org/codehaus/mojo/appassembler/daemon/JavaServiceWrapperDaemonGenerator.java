package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.generic.Daemon;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role-hint="jsw"
 */
public class JavaServiceWrapperDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{
    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    public void generate( Daemon stubDaemon, MavenProject project, ArtifactRepository localRepository,
                          File outputDirectory )
        throws DaemonGeneratorException
    {
        InputStream in = this.getClass().getResourceAsStream( "wrapper.conf.template" );

        InputStreamReader reader = new InputStreamReader( in );

        Map context = new HashMap();
        context.put( "MAINCLASS", stubDaemon.getMainClass() );
        context.put( "CLASSPATH", constructClasspath( project ) );
        context.put( "ADDITIONAL", "" );
        context.put( "INITIAL_MEMORY", "" );
        context.put( "MAX_MEMORY", "" );
        context.put( "PARAMETERS", createParameters( stubDaemon, project ) );

        InterpolationFilterReader interpolationFilterReader = new InterpolationFilterReader( reader, context, "@", "@" );

        outputDirectory = new File( outputDirectory, "etc" );
        File outputFile = new File( outputDirectory, "wrapper.conf" );
        FileWriter out = null;

        try
        {
            // -----------------------------------------------------------------------
            // Make the parent directories
            // -----------------------------------------------------------------------

            FileUtils.forceMkdir( outputDirectory );

            // -----------------------------------------------------------------------
            // Write the file
            // -----------------------------------------------------------------------

            out = new FileWriter( outputFile );

            IOUtil.copy( interpolationFilterReader, out );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error writing output file: " + outputFile.getAbsolutePath(), e );
        }
        finally
        {
            IOUtil.close(interpolationFilterReader);
            IOUtil.close(out);
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private String constructClasspath( MavenProject project )
    {
        StringWriter string = new StringWriter();

        PrintWriter writer = new PrintWriter( string );

        writer.println( "wrapper.java.classpath.1=lib/wrapper.jar" );
        writer.println( "wrapper.java.classpath.2=../../repo/" + Util.getRelativePath( project.getArtifact() ) );
        int i = 3;
        for ( Iterator it = project.getRuntimeArtifacts().iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            String path = Util.getRelativePath( artifact );
            writer.println( "wrapper.java.classpath." + i + "=../../repo/" + path );
            i++;
        }

        return string.toString();
    }

    private String createParameters( Daemon stubDaemon, MavenProject project )
    {
        return "wrapper.app.parameter.1=" + stubDaemon.getMainClass();
    }
}
