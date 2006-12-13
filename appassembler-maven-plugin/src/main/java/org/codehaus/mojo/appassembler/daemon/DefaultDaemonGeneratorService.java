package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.io.xpp3.AppassemblerModelXpp3Reader;
import org.codehaus.mojo.appassembler.daemon.merge.DaemonMerger;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component
 */
public class DefaultDaemonGeneratorService
    extends AbstractLogEnabled
    implements DaemonGeneratorService
{
    /**
     * @plexus.requirement role=org.codehaus.mojo.appassembler.daemon.DaemonGenerator
     */
    private Map generators;

    /**
     * @plexus.requirement
     */
    private DaemonMerger daemonMerger;

    // -----------------------------------------------------------------------
    // DaemonGeneratorService Implementation
    // -----------------------------------------------------------------------

    public void generateDaemon( String platform, File stubDescriptor, File outputDirectory, MavenProject mavenProject,
                                ArtifactRepository localRepository )
        throws DaemonGeneratorException
    {
        generateDaemon( platform, stubDescriptor, null, outputDirectory, mavenProject, localRepository );
    }

    public void generateDaemon( String platform, File stubDescriptor, Daemon stubDaemon,
                                File outputDirectory, MavenProject mavenProject, ArtifactRepository localRepository )
        throws DaemonGeneratorException
    {
        // -----------------------------------------------------------------------
        // Get the generator
        // -----------------------------------------------------------------------

        DaemonGenerator generator = (DaemonGenerator) generators.get( platform );

        if ( generator == null )
        {
            throw new DaemonGeneratorException( "Could not find a generator for platform '" + platform + "'." );
        }

        // -----------------------------------------------------------------------
        // Load the model
        // -----------------------------------------------------------------------

        Daemon fileDaemon = null;

        if ( stubDescriptor != null )
        {
            getLogger().debug( "Loading daemon descriptor: " + stubDescriptor.getAbsolutePath() );

            fileDaemon = loadModel( stubDescriptor );
        }

        // -----------------------------------------------------------------------
        // Merge the given stub daemon
        // -----------------------------------------------------------------------

        Daemon mergedDaemon = mergeDaemons( stubDaemon, fileDaemon );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        validateDaemon( mergedDaemon, stubDescriptor );

        // -----------------------------------------------------------------------
        // Generate!
        // -----------------------------------------------------------------------

        generator.generate( mergedDaemon, mavenProject, localRepository, outputDirectory );
    }

    public Daemon mergeDaemons( Daemon dominant, Daemon recessive )
        throws DaemonGeneratorException
    {
        return daemonMerger.mergeDaemons( dominant, recessive );
    }

    public Daemon loadModel( File stubDescriptor )
        throws DaemonGeneratorException
    {
        AppassemblerModelXpp3Reader reader = new AppassemblerModelXpp3Reader();

        Daemon stubDaemon;

        try
        {
            stubDaemon = reader.read( new FileReader( stubDescriptor ) );
        }
        catch ( XmlPullParserException e )
        {
            throw new DaemonGeneratorException( "Error parsing " + stubDescriptor.getAbsolutePath(), e );
        }
        catch ( FileNotFoundException e )
        {
            throw new DaemonGeneratorException( "No such file: " + stubDescriptor.getAbsolutePath() );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error while reading: " + stubDescriptor.getAbsolutePath() );
        }

        validateDaemon( stubDaemon, stubDescriptor );

        return stubDaemon;
    }

    public void validateDaemon( Daemon daemon, File descriptor )
        throws DaemonGeneratorException
    {
        if ( daemon == null )
        {
            throw new DaemonGeneratorException( "Illegal argument: daemon must be passed." );
        }

        String mainClass = daemon.getMainClass();

        String missingRequiredField;

        if ( descriptor != null )
        {
            missingRequiredField = "Missing required field from '" + descriptor.getAbsolutePath() + "': ";
        }
        else
        {
            missingRequiredField = "Missing required field in daemon descriptor: ";
        }

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        if ( StringUtils.isEmpty( mainClass ) )
        {
            throw new DaemonGeneratorException( missingRequiredField + "main class." );
        }

        if ( StringUtils.isEmpty( daemon.getId() ) )
        {
            String id = mainClass;

            int i = id.lastIndexOf( '.' );

            if ( i > 0 )
            {
                id = mainClass.substring( i + 1 );
            }

            id = StringUtils.addAndDeHump( id );

            daemon.setId( id );
        }
    }
}
