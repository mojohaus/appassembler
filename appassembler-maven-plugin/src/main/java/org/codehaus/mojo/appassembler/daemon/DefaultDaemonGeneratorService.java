package org.codehaus.mojo.appassembler.daemon;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.mojo.appassembler.model.generic.io.xpp3.GenericApplicationModelXpp3Reader;
import org.codehaus.mojo.appassembler.model.generic.Daemon;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component
 */
public class DefaultDaemonGeneratorService
    extends AbstractLogEnabled
    implements DaemonGeneratorService
{
    /**
     * @plexus.requirement role=DaemonGenerator
     */
    private Map generators;

    // -----------------------------------------------------------------------
    // DaemonGeneratorService Implementation
    // -----------------------------------------------------------------------

    public void generateDaemon( String platform, File stubDescriptor, File outputDirectory, MavenProject mavenProject,
                                ArtifactRepository localRepository )
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

        getLogger().debug( "Loading daemon descriptor: " + stubDescriptor.getAbsolutePath() );

        Daemon stubDaemon = loadModel( stubDescriptor );

        // -----------------------------------------------------------------------
        // Generate!
        // -----------------------------------------------------------------------

        generator.generate( stubDaemon, mavenProject, localRepository, outputDirectory );
    }

    public Daemon loadModel( File stubDescriptor )
        throws DaemonGeneratorException
    {
        GenericApplicationModelXpp3Reader reader = new GenericApplicationModelXpp3Reader();

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

        // -----------------------------------------------------------------------
        // Validate
        // -----------------------------------------------------------------------

        String mainClass = stubDaemon.getMainClass();

        if ( StringUtils.isEmpty( mainClass ) )
        {
            throw new DaemonGeneratorException( "Missing required field from '" + stubDescriptor.getAbsolutePath() + "': main class." );
        }

        if ( StringUtils.isEmpty( stubDaemon.getId() ) )
        {
            String id = mainClass;

            int i = id.lastIndexOf( '.' );

            if ( i > 0 )
            {
                id = mainClass.substring( i + 1 );
            }

            id = StringUtils.addAndDeHump( id );

            stubDaemon.setId( id );
        }

        return stubDaemon;
    }
}
