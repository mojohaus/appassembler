package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.merge.DaemonMerger;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.io.DaemonModelUtil;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
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
        DaemonGenerationRequest request = new DaemonGenerationRequest();

        request.setPlatform( platform );
        request.setStubDescriptor( stubDescriptor );
        request.setStubDaemon( stubDaemon );
        request.setOutputDirectory( outputDirectory );
        request.setMavenProject( mavenProject );
        request.setLocalRepository( localRepository );

        generateDaemon( request );
    }

    public void generateDaemon( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        String platform = request.getPlatform();

        if ( platform == null || StringUtils.isEmpty( platform ) )
        {
            throw new DaemonGeneratorException( "Missing required property in request: platform." );
        }

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

        File stubDescriptor = request.getStubDescriptor();

        if ( stubDescriptor != null )
        {
            getLogger().debug( "Loading daemon descriptor: " + stubDescriptor.getAbsolutePath() );

            fileDaemon = loadModel( stubDescriptor );
        }

        // -----------------------------------------------------------------------
        // Merge the given stub daemon
        // -----------------------------------------------------------------------

        Daemon mergedDaemon = mergeDaemons( request.getStubDaemon(), fileDaemon );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        validateDaemon( mergedDaemon, stubDescriptor );

        // -----------------------------------------------------------------------
        // Generate!
        // -----------------------------------------------------------------------

        request.setDaemon( mergedDaemon );

        generator.generate( request );
    }

    public Daemon mergeDaemons( Daemon dominant, Daemon recessive )
        throws DaemonGeneratorException
    {
        return daemonMerger.mergeDaemons( dominant, recessive );
    }

    public Daemon loadModel( File stubDescriptor )
        throws DaemonGeneratorException
    {
        try
        {
            Daemon stubDaemon = DaemonModelUtil.loadModel( stubDescriptor );

            validateDaemon( stubDaemon, stubDescriptor );

            return stubDaemon;
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error while loading daemon descriptor from '" + stubDescriptor.getAbsolutePath() + "'.", e );
        }
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
