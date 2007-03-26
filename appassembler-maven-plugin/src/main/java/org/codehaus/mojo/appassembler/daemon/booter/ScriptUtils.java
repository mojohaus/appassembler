package org.codehaus.mojo.appassembler.daemon.booter;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Daemon;
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

public class ScriptUtils
{
    public static void createBinScript( boolean isWindows, Daemon descriptor, MavenProject project,
                                        ArtifactRepository localRepository, File outputDirectory )
        throws DaemonGeneratorException
    {
        try
        {
            InputStream in = ScriptUtils.class.getResourceAsStream( PlatformUtil.getBinTemplate( isWindows ) );

            InputStreamReader reader = new InputStreamReader( in );

            Map context = new HashMap();
            context.put( "MAINCLASS", "org.codehaus.mojo.appassembler.booter.AppassemblerBooter" );
            context.put( "CLASSPATH", PlatformUtil.getClassPath( true, true, isWindows, project, localRepository ) );
            context.put( "EXTRA_JVM_ARGUMENTS",
                         PlatformUtil.getExtraJvmArgumentsForCli( descriptor.getJvmSettings() ) );
            context.put( "APP_NAME", descriptor.getId() );
            context.put( "APP_ARGUMENTS", PlatformUtil.getAppArguments( descriptor ) );

            String interpolationToken = PlatformUtil.getInterpolationToken( isWindows );
            InterpolationFilterReader interpolationFilterReader = new InterpolationFilterReader( reader, context,
                                                                                                 interpolationToken,
                                                                                                 interpolationToken );

            // Set the name of the bin file
            String programName = "";

            if ( descriptor.getId() == null || descriptor.getId().trim().equals( "" ) )
            {
                // Get class name and use it as the filename
                StringTokenizer tokenizer = new StringTokenizer( descriptor.getMainClass(), "." );
                while ( tokenizer.hasMoreElements() )
                {
                    programName = tokenizer.nextToken();
                }

                programName = programName.toLowerCase();
            }
            else
            {
                programName = descriptor.getId();
            }

            String binFileName = programName + PlatformUtil.getBinFileExtension( isWindows );

            File binFile = createBinFile( outputDirectory, binFileName );

            FileWriter out = new FileWriter( binFile );

            try
            {
                IOUtil.copy( interpolationFilterReader, out );
            }
            finally
            {
                IOUtil.close( interpolationFilterReader );
                IOUtil.close( out );
            }
        }
        catch ( FileNotFoundException e )
        {
            throw new DaemonGeneratorException( "Failed to get template for bin file.", e );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Failed to write bin file.", e );
        }
    }

    private static File createBinFile( File outputDirectory, String binFileName )
        throws IOException
    {
        FileUtils.forceMkdir( outputDirectory );
        File binDir = new File( outputDirectory, "bin" );
        FileUtils.forceMkdir( binDir );
        return new File( binDir, binFileName );
    }
}
