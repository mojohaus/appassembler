package org.codehaus.mojo.appassembler;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.repository.layout.LegacyRepositoryLayout;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.appassembler.repository.FlatRepositoryLayout;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Util
{
    private static ArtifactRepositoryLayout defaultLayout = new DefaultRepositoryLayout();

    public static String getRelativePath( Artifact artifact, String layout )
    {
        ArtifactRepositoryLayout repositoryLayout;

        try
        {
            repositoryLayout = getRepositoryLayout( layout );
        }
        catch ( MojoFailureException e )
        {
            repositoryLayout = defaultLayout;
        }

        return getRelativePath( artifact, repositoryLayout );
    }

    public static String getRelativePath( Artifact artifact, ArtifactRepositoryLayout layout )
    {
        return layout.pathOf( artifact );
    }
    
    public static ArtifactRepositoryLayout getRepositoryLayout( String layout )
        throws MojoFailureException
    {
        ArtifactRepositoryLayout repositoryLayout;

        if ( layout == null || layout.equals( "default" ) )
        {
            repositoryLayout = new DefaultRepositoryLayout();
        }
        else if ( layout.equals( "legacy" ) )
        {
            repositoryLayout = new LegacyRepositoryLayout();
        }
        else if ( layout.equals( "flat" ) )
        {
            repositoryLayout = new FlatRepositoryLayout();
        }
        else
        {
            throw new MojoFailureException( "Unknown repository layout '" + layout + "'." );
        }

        return repositoryLayout;
    }
}
